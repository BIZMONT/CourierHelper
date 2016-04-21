package com.bizmont.courierhelper.Services;


import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bizmont.courierhelper.Activities.WarehouseActivity;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.OtherStuff.CourierState;
import com.bizmont.courierhelper.Point.Point;
import com.bizmont.courierhelper.Point.WarehousePoint;
import com.bizmont.courierhelper.R;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GPSTrackerService extends Service implements LocationListener {
    //constants
    public static final String BROADCAST_SEND_ACTION = "com.bizmont.courierhelper.cordinates"; //filter to map activity
    public static final String BROADCAST_RECEIVE_ACTION = "com.bizmont.courierhelper.actions";
    public static final int LOCATION_REFRESH_RATE = 0; //seconds
    public static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    public static final float LOCATION_MIN_ACCURACY = 50;
    public static final int LOCATION_MIN_SATELLITES = 4;

    private static final String LOG_TAG = "GPSTracker";

    //location fields
    private LocationManager locationManager;
    private GpsStatus gpsStatus;
    private Location lastFix;
    private String nmea;

    private boolean isTracked;
    private boolean isLocationDisabled;

    NotificationCompat.Builder nearPointNotify;
    NotificationCompat.Builder locationDisabledNotify;
    NotificationManager notificationManager;

    ArrayList<Point> points;

    BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        isTracked = true;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getBooleanExtra("Update points",false))
                {
                    Log.d(LOG_TAG, "Update points");
                    points = DataBase.getPoints();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_RECEIVE_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Location disabled notification
        locationDisabledNotify = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_location_off_notify)
                .setContentTitle(getString(R.string.location_disabled))
                .setContentText(getString(R.string.location_disabled_message));
        Intent resultIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        locationDisabledNotify.setContentIntent(resultPendingIntent);

        //Service status notification
        NotificationCompat.Builder serviceStatusNotify = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_service_active_notify)
                .setContentTitle("Courier Helper")
                .setContentText(getString(R.string.service_work))
                .setOngoing(true);
        notificationManager.notify(002, serviceStatusNotify.build());

        //Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    GPSTrackerService.this.nmea = nmea;
                }
            });
            locationManager.addGpsStatusListener(new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {

                }
            });
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * LOCATION_REFRESH_RATE, LOCATION_REFRESH_DISTANCE, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * LOCATION_REFRESH_RATE, LOCATION_REFRESH_DISTANCE, this);
        }

        //Last successful fix initialization
        lastFix = new Location(LocationManager.GPS_PROVIDER);
        lastFix.setLatitude(0);
        lastFix.setLongitude(0);

        points = DataBase.getPoints();

        Log.d(LOG_TAG, "Service onCreate");
    }
    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        Log.d(LOG_TAG, "Service onDestroy");
        notificationManager.cancel(001);
        notificationManager.cancel(002);
        notificationManager.cancel(003);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    //Service overridden methods
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(LOG_TAG, "Service onBind");
        sendLocationInfo(lastFix, getSatellitesInUse());
        checkPoints(lastFix);
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        if(!isTracked)
        {
            stopSelf();
        }
        Log.d(LOG_TAG, "Service onUnbind");
        return true;
    }

    //LocationListener overridden methods
    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"onLocationChanged");
        getLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        isLocationDisabled = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation(locationManager.getLastKnownLocation(provider));
        }
    }
    @Override
    public void onProviderDisabled(String provider)
    {
        checkEnabled();
    }

    //Own methods
    private void checkEnabled()
    {
        if(!isLocationDisabled) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                isLocationDisabled = true;
                notificationManager.notify(001, locationDisabledNotify.build());
            }
        }
    }

    private int getSatellitesInUse()
    {
        locationManager.getGpsStatus(null);
        gpsStatus = locationManager.getGpsStatus(gpsStatus);
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        int inUse = 0;
        if (satellites != null) {
            for (GpsSatellite gpsSatellite : satellites) {
                if (gpsSatellite.usedInFix()) {
                    inUse++;
                }
            }
        }
        return inUse;
    }

    private void getLocation(Location location)
    {
        if(location == null) {
            return;
        }

        Log.d(LOG_TAG,"Location received: " + location.getLatitude() + " " + location.getLongitude());

        if(checkLocation(location))
        {
            checkPoints(location);
        }
    }
    private void sendLocationInfo(Location location, int satellitesInUse)
    {
        Intent locationIntent = new Intent(BROADCAST_SEND_ACTION);

        locationIntent.putExtra("location", location);
        locationIntent.putExtra("nmea", nmea);
        locationIntent.putExtra("satellitesInUse", satellitesInUse);
        locationIntent.putExtra("isTracked",isTracked);

        sendBroadcast(locationIntent);
        Log.d(LOG_TAG, "Location sent (" + location.getLatitude() + " " + location.getLongitude() + ")");
    }

    private boolean checkLocation(Location location)
    {
        int satellitesInUse = getSatellitesInUse();
        if (location.getAccuracy() < LOCATION_MIN_ACCURACY &&
                lastFix.distanceTo(location) > 7)
        {
            if(location.getProvider() == "gps" &&  satellitesInUse < LOCATION_MIN_SATELLITES)
            {
                return false;
            }
            lastFix = location;
            sendLocationInfo(location, satellitesInUse);
            return true;
        }
        return false;
    }

    private void checkPoints(Location location)
    {
        if (nearPointNotify != null)
        {
            notificationManager.cancel(003);
        }

        for(Point point : points)
        {
            if(new GeoPoint(lastFix).distanceTo(new GeoPoint(point.getLatitude(), point.getLongitude())) <= point.getRadius() + location.getAccuracy())
            {
                if(point.getClass() == WarehousePoint.class)
                {
                    Courier.getInstance().setState(CourierState.IN_WAREHOUSE);

                    Intent resultIntent = new Intent(this, WarehouseActivity.class);
                    resultIntent.putExtra("ID", point.getID());
                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(this, (int) System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    nearPointNotify = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_warehouse_notify)
                            .setContentTitle("Courier Helper")
                            .setContentText("You are near warehouse #" + point.getID())
                            .addAction(R.drawable.ic_warehouse,"Open",resultPendingIntent);
                }
                else
                {
                    Courier.getInstance().setState(CourierState.AT_THE_POINT);

                    nearPointNotify = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_task_notify)
                            .setContentTitle("Courier Helper")
                            .setContentText("You are near target #" + point.getID());

                        /*Intent resultIntent = new Intent(this, Warehouse);
                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        this,
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        locationDisabledNotify.setContentIntent(resultPendingIntent);*/
                }

                notificationManager.notify(003, nearPointNotify.build());
            }
        }
    }
}