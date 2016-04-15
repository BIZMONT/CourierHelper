package com.bizmont.courierhelper.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTrackerService;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = "CourierHelperLog";

    private long backPressedTime = 0;
    private boolean isTracked = false;

    private TextView locationInfo;
    private MapView map;
    private IMapController mapController;

    private NavigationView navigationView;
    private Marker userLocationMarker;
    private Polygon accuracyRadius;

    private Polyline route;
    private ArrayList<GeoPoint> points;
    private GeoPoint userLocationGeoPoint;

    private int satellitesInUse;
    private String nmeaString;

    private BroadcastReceiver broadcastReceiver;

    //Overrated methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        setTitle(R.string.title_activity_map);

        //receiving location from service
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Location location = intent.getParcelableExtra("location");

                nmeaString = intent.getStringExtra("nmea");
                satellitesInUse = intent.getIntExtra("satellitesInUse", 0);
                isTracked = intent.getBooleanExtra("isTracked", false);

                Log.d(LOG_TAG, "locationReceived");
                showMarkerOnMap(location);
            }
        };

        //map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(2);
        map.setBuiltInZoomControls(false);

        mapController = map.getController();
        mapController.setZoom(3);


        //location elements
        userLocationMarker = new Marker(map);
        userLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.user_position_marker));
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        map.getOverlays().add(userLocationMarker);

        route = new Polyline(this);
        points = new ArrayList<>();
        map.getOverlays().add(route);

        accuracyRadius = new Polygon(this);
        accuracyRadius.setStrokeWidth(1);
        accuracyRadius.setStrokeColor(Color.BLUE);
        map.getOverlays().add(accuracyRadius);


        //Interface items
        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_map).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
            ((TextView) headerView.findViewById(R.id.courier_name)).setText(Courier.getInstance().getName());
            ((TextView) headerView.findViewById(R.id.courier_status)).setText(Courier.getInstance().getState().toString());

        //Info Overlay
        nmeaString = "";
        locationInfo = (TextView) findViewById(R.id.map_info);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!navigationView.getMenu().findItem(R.id.nav_map).isChecked()) {
            navigationView.getMenu().findItem(R.id.nav_map).setChecked(true);
        }
        IntentFilter intentFilter = new IntentFilter(GPSTrackerService.BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }
    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
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
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_get_tasks) {
            Intent intent = new Intent(this, GetTasksActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_reports) {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Own methods
    private void showMarkerOnMap(Location location)
    {
        if(userLocationGeoPoint == null)
        {
            mapController.setZoom(17);
            mapController.animateTo(new GeoPoint(location));
        }
        showPositionInfo(location);

        userLocationGeoPoint = new GeoPoint(location);
        userLocationMarker.setPosition(userLocationGeoPoint);
        userLocationMarker.setTitle(Courier.getInstance().getName() + " " + Courier.getInstance().getState());
        userLocationMarker.setSubDescription(convertPointToAddress(userLocationGeoPoint));
        accuracyRadius.setPoints(Polygon.pointsAsCircle(userLocationGeoPoint, location.getAccuracy()));

        if(isTracked) {
            points.add(userLocationGeoPoint);
            route.setPoints(points);
        }

        map.invalidate();
    }
    public void showPositionInfo(Location location)
    {
        locationInfo.setText("Coordinates:\n  lat: " + location.getLatitude() +
                "\n  lon: " + location.getLongitude() +
                "\nAccuracy: " + location.getAccuracy() +
                "\nSpeed: " + location.getSpeed() +
                "\nTime: " + Calendar.getInstance().getTime() +
                "\nNMEA: " + nmeaString +
                "\nSatellites in use: " + satellitesInUse);
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
}