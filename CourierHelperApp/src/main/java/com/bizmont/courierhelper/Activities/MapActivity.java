package com.bizmont.courierhelper.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Courier.Courier;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.Point.Point;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.RoadFile;
import com.bizmont.courierhelper.Services.GPSTracker;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String LOG_TAG = "CourierHelperLog";
    private static final int MAP_MIN_ZOOM_LEVEL = 2;
    private static final int DEFAULT_ZOOM_LEVEL = 3;
    private static final int MAP_MAX_ZOOM_LEVEL = 19;

    private long backPressedTime = 0;

    private MapView map;
    private IMapController mapController;

    //Overlays
    private NavigationView navigationView;
    private Marker userLocationMarker;
    private Polygon accuracyRadius;
    private FolderOverlay markersOverlays;
    private GeoPoint userLocationGeoPoint;
    private FolderOverlay pathOverlay;

    private BroadcastReceiver broadcastReceiver;

    SensorManager mSensorManager;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;

    //Overrated methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        setTitle(R.string.title_activity_map);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        serviceIntent = new Intent(this,GPSTracker.class);
        startService(serviceIntent);

        rotationSensorInit();
        broadcastReceiverInit();

        mapInit();

        //Overlays
        accuracyRadiusOverlayInit();
        userMarkerOverlayInit();

        Courier.addOnStatusChangedListener(new Courier.CourierListener() {
            @Override
            public void onStatusChanged()
            {
                refreshCourierInfo();
            }
        });

        //Interface items

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ImageButton showLocation = (ImageButton) findViewById(R.id.show_current_location);
        assert showLocation != null;
        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userLocationGeoPoint != null)
                {
                    mapController.setZoom(17);
                    mapController.animateTo(userLocationGeoPoint);
                }
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_map).setChecked(true);

    }

    private void mapInit()
    {
        map = (MapView) findViewById(R.id.map);
        assert map != null;
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(MAP_MIN_ZOOM_LEVEL);
        map.setMaxZoomLevel(MAP_MAX_ZOOM_LEVEL);
        map.setBuiltInZoomControls(false);

        mapController = map.getController();
        mapController.setZoom(DEFAULT_ZOOM_LEVEL);
    }
    private void accuracyRadiusOverlayInit()
    {
        accuracyRadius = new Polygon(this);
        accuracyRadius.setStrokeColor(Color.parseColor("#002196f3"));
        accuracyRadius.setFillColor(Color.parseColor("#442196f3"));
        map.getOverlays().add(accuracyRadius);
    }
    private void broadcastReceiverInit()
    {
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if(intent.getBooleanExtra(ExtrasNames.IS_LOCATION,false))
                {
                    Location location = intent.getParcelableExtra(ExtrasNames.LOCATION);

                    boolean isRefresh = intent.getBooleanExtra(ExtrasNames.IS_REFRESH, false);
                    if(isRefresh)
                    {
                        if(markersOverlays != null)
                        {
                            map.getOverlays().remove(markersOverlays);
                        }
                        markersOverlays = getMapPointsOverlay(getApplicationContext(), map);
                        map.getOverlays().add(markersOverlays);
                    }

                    Log.d(LOG_TAG, "locationReceived");
                    if(location.getLatitude() != 0 && location.getLongitude() !=0) {
                        showLocationMarkerOnMap(location);
                    }
                }
                if(intent.getBooleanExtra(ExtrasNames.IS_PATH_UPDATE,false))
                {
                    if(pathOverlay != null)
                    {
                        map.getOverlays().remove(pathOverlay);
                    }
                    File overlayFile = new File( getFilesDir() + "/kml", "recommended_path.kml");
                    pathOverlay = RoadFile.getOverlayFromFile(getApplicationContext(),overlayFile,map);
                    map.getOverlays().add(pathOverlay);
                    map.invalidate();
                }
            }
        };
    }
    private void rotationSensorInit()
    {
        SensorEventListener mSensorListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                float mOrientation = event.values[0];
                userLocationMarker.setRotation(mOrientation);
                map.invalidate();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }
    private void userMarkerOverlayInit()
    {
        userLocationMarker = new Marker(map);
        userLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map_location));
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        userLocationMarker.setFlat(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(GPSTracker.BROADCAST_SEND_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        if (!navigationView.getMenu().findItem(R.id.nav_map).isChecked()) {
            navigationView.getMenu().findItem(R.id.nav_map).setChecked(true);
        }

        //Points update
        if(markersOverlays != null)
        {
            map.getOverlays().remove(markersOverlays);
        }
        markersOverlays = getMapPointsOverlay(getApplicationContext(), map);
        map.getOverlays().add(markersOverlays);

        //Path update
        if(pathOverlay != null)
        {
            map.getOverlays().remove(pathOverlay);
        }
        File overlayFile = new File( getFilesDir() + "/kml", "recommended_path.kml");
        pathOverlay = RoadFile.getOverlayFromFile(getApplicationContext(),overlayFile,map);
        map.getOverlays().add(pathOverlay);

        map.invalidate();

        //Courier status refresh
        refreshCourierInfo();

        Log.d(LOG_TAG, "MapActivity onResume");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "MapActivity onPause");
        unbindService(serviceConnection);
    }
    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "MapActivity onDestroy");
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int taskID = intent.getIntExtra(ExtrasNames.TASK_ID, 0);
        Log.d(LOG_TAG,"Showing task #" + String.valueOf(taskID));
        if(taskID != 0)
        {
            for(Overlay overlay : markersOverlays.getItems())
            {
                if(!(overlay instanceof Marker))
                {
                    continue;
                }
                Marker marker = (Marker) overlay;
                if(marker.getTitle().contains(Integer.toString(taskID)))
                {
                    mapController.setZoom(17);
                    mapController.animateTo(marker.getPosition());
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        long t = System.currentTimeMillis();
        if (t - backPressedTime > 2000)
        {
            backPressedTime = t;
            Toast.makeText(this, R.string.toast_exit_message, Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_reports) {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.nav_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.nav_about)
        {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Own methods
    private void showLocationMarkerOnMap(Location location)
    {
        if(userLocationGeoPoint == null)
        {
            map.getOverlays().add(userLocationMarker);
            mapController.setZoom(MAP_MAX_ZOOM_LEVEL);
            mapController.animateTo(new GeoPoint(location));
        }
        //showPositionInfo(location);

        userLocationGeoPoint = new GeoPoint(location);
        userLocationMarker.setPosition(userLocationGeoPoint);
        userLocationMarker.setTitle(Courier.getInstance().getName() + " " + Courier.getInstance().getState());
        userLocationMarker.setSubDescription(convertPointToAddress(userLocationGeoPoint));
        accuracyRadius.setPoints(Polygon.pointsAsCircle(userLocationGeoPoint, location.getAccuracy()));

        map.invalidate();
    }
    public String convertPointToAddress(GeoPoint point) {
        String address = "";
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6, 1);

            if (addresses.size() > 0) {
                for (int index = 0; index < addresses.get(0).getMaxAddressLineIndex(); index++) {
                    address += addresses.get(0).getAddressLine(index) + " ";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    private FolderOverlay getMapPointsOverlay(Context context, MapView mapView)
    {
        ArrayList<Point> points = DataBase.getTargetPoints();

        FolderOverlay pointsFolderOverlay = new FolderOverlay(context);
        for(Point point : points)
        {
            Polygon radius = new Polygon(context);
            radius.setStrokeColor(Color.parseColor("#00000000"));
            radius.setFillColor(Color.parseColor("#66aaaaaa"));
            radius.setPoints(Polygon.pointsAsCircle(
                    new GeoPoint(point.getLatitude(),point.getLongitude()),
                    point.getRadius()));

            pointsFolderOverlay.add(radius);
            pointsFolderOverlay.add(point.createMarker(context, mapView));
        }
        return pointsFolderOverlay;
    }
    private void refreshCourierInfo()
    {
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.courier_name)).setText(Courier.getInstance().getName());
        ((TextView) headerView.findViewById(R.id.courier_status)).setText(Courier.getInstance().getState().toString());
    }
}