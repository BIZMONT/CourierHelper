package com.bizmont.courierhelper;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    final int DIALOG_SETTING = 1;
    private LocationManager locationManager;
    private MapView map;
    private IMapController mapController;
    ArrayList<OverlayItem> overlayItemArray;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        InitMap();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            showDialog(DIALOG_SETTING);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    private void InitMap()
    {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(2);

        mapController = map.getController();
        mapController.setZoom(3);

        overlayItemArray = new ArrayList<OverlayItem>();

        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
        UserPositionItemizedIconOverlay myItemizedIconOverlay = new UserPositionItemizedIconOverlay(overlayItemArray, null, defaultResourceProxyImpl);
        map.getOverlays().add(myItemizedIconOverlay);

        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        map.getOverlays().add(myScaleBarOverlay);
    }


    private void showLocation(Location location)
    {
        GeoPoint userPosition = new GeoPoint(location);
        Toast.makeText(getBaseContext(),
                "Latitude: " + location.getLatitude() +
                        " Longitude: " + location.getLongitude(),
                Toast.LENGTH_SHORT).show();

        overlayItemArray.clear();

        OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", userPosition);
        overlayItemArray.add(newMyLocationItem);

        //mapController.animateTo(locGeoPoint);
        //mapController.setZoom(16);

        map.invalidate();
    }


    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_job)
        {

        }
        else if (id == R.id.nav_map)
        {

        }
        else if (id == R.id.nav_slideshow)
        {

        }
        else if (id == R.id.nav_manage)
        {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            showLocation(location);
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider)
        {
            showLocation(locationManager.getLastKnownLocation(provider));
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    };
    private class UserPositionItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem>
    {
        public UserPositionItemizedIconOverlay(List<OverlayItem> pList, OnItemGestureListener<OverlayItem> pOnItemGestureListener, ResourceProxy pResourceProxy)
        {
            super(pList, pOnItemGestureListener, pResourceProxy);
        }

        @Override
        public void draw(Canvas canvas, MapView mapview, boolean arg2)
        {
            super.draw(canvas, mapview, arg2);

            if(!overlayItemArray.isEmpty())
            {
                IGeoPoint in = overlayItemArray.get(0).getPoint();

                Point out = new Point();
                mapview.getProjection().toPixels(in, out);

                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_current);
                canvas.drawBitmap(bm, out.x - bm.getWidth()/2, out.y - bm.getHeight()/2, null);
            }
        }
        @Override
        public boolean onSingleTapUp(MotionEvent event, MapView mapView)
        {
            return true;
        }
    }

    protected Dialog onCreateDialog(int id)
    {
        if (id == DIALOG_SETTING)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);

            adb.setTitle(R.string.dialog_setting_title);
            adb.setMessage(R.string.dialog_setting_message);
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton(R.string.dialog_setting_yes, myClickListener);
            adb.setNeutralButton(R.string.dialog_setting_cancel, myClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {

                case Dialog.BUTTON_POSITIVE:
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };
}
