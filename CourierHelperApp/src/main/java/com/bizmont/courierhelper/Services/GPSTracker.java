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

import com.bizmont.courierhelper.Activities.CompleteTaskActivity;
import com.bizmont.courierhelper.Activities.WarehouseActivity;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.OtherStuff.CourierState;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.Point.DeliveryPoint;
import com.bizmont.courierhelper.Point.Point;
import com.bizmont.courierhelper.Point.WarehousePoint;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.PathBuilderTask;
import com.bizmont.courierhelper.Task.TaskState;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class GPSTracker extends Service implements LocationListener
{
    //constants
    public static final String BROADCAST_SEND_ACTION = "com.bizmont.courierhelper.coordinates"; //filter to map activity
    public static final String BROADCAST_RECEIVE_ACTION = "com.bizmont.courierhelper.actions"; //filter to service

    public static final int LOCATION_REFRESH_RATE = 0; //seconds
    public static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    public static final float LOCATION_MIN_ACCURACY = 50;
    public static final int LOCATION_MIN_SATELLITES = 4;

    public static final int LOCATION_ALERT_NOTIFICATION_ID = 1;
    public static final int STATE_NOTIFICATION_ID = 2;
    public static final int POINT_NOTIFICATION_ID = 3;

    private static final String LOG_TAG = "GPS Tracker Service";

    //location fields
    private LocationManager locationManager;
    private GpsStatus gpsStatus;
    private Location lastFix;
    private String nmea;

    private boolean isTracked;
    private boolean isLocationDisabled;

    NotificationCompat.Builder nearPointNotify;
    NotificationManager notificationManager;

    ArrayList<Point> points;
    ArrayList<Road> path;

    BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        isTracked = true;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getBooleanExtra(ExtrasNames.IS_UPDATE_POINTS,false))
                {
                    points = DataBase.getTargetPoints();
                    Log.d(LOG_TAG, "Point updated");
                }
                if (intent.getBooleanExtra(ExtrasNames.IS_CREATE_ROUTE,false))
                {
                    path = buildOptimalPath();
                    Log.d(LOG_TAG, "Route created");
                    sendPathBroadcast();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_RECEIVE_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Service status notification
        showServiceStatusNotify();

        //Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    GPSTracker.this.nmea = nmea;
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

        points = DataBase.getTargetPoints();

        Log.d(LOG_TAG, "Service onCreate");
    }
    @Override
    public void onDestroy()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }

        hideLocationAlertNotify();
        hideServiceStateNotify();
        hideOnPointNotify();

        unregisterReceiver(broadcastReceiver);

        Log.d(LOG_TAG, "Service onDestroy");
        super.onDestroy();
    }

    //Service overridden methods
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(LOG_TAG, "Service onBind");
        sendLocationBroadcast(lastFix, getSatellitesInUse());
        checkPoints(lastFix);

        if (path !=null)
        {
            sendPathBroadcast();
        }

        return new Binder();
    }
    @Override
    public void onRebind(Intent intent)
    {
        Log.d(LOG_TAG, "Service onRebind");
        super.onRebind(intent);
        sendLocationBroadcast(lastFix, getSatellitesInUse());
        checkPoints(lastFix);

        if (path !=null)
        {
            sendPathBroadcast();
        }
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
        hideLocationAlertNotify();
        isLocationDisabled = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation(locationManager.getLastKnownLocation(provider));
        }
    }
    @Override
    public void onProviderDisabled(String provider)
    {
        checkProvidersAvailability();
    }

    //Own methods
    private void checkProvidersAvailability()
    {
        if(!isLocationDisabled) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                isLocationDisabled = true;
                showLocationAlertNotify();
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

        Log.d(LOG_TAG,"Location received: " + location.getLatitude() + " " + location.getLongitude() + " " + getSatellitesInUse());

        if(isLocationValid(location))
        {
            checkPoints(location);
        }
    }
    private void sendLocationBroadcast(Location location, int satellitesInUse)
    {
        Intent locationIntent = new Intent(BROADCAST_SEND_ACTION);

        locationIntent.putExtra(ExtrasNames.IS_LOCATION, true);
        locationIntent.putExtra(ExtrasNames.LOCATION, location);
        locationIntent.putExtra(ExtrasNames.NMEA, nmea);
        locationIntent.putExtra(ExtrasNames.SATELLITES_IN_USE, satellitesInUse);
        locationIntent.putExtra(ExtrasNames.IS_TRACK,isTracked);

        sendBroadcast(locationIntent);
        Log.d(LOG_TAG, "Location sent (" + location.getLatitude() + " " + location.getLongitude() + ")");
    }
    private void sendPathBroadcast()
    {
        Intent pathIntent = new Intent(BROADCAST_SEND_ACTION);
        pathIntent.putExtra(ExtrasNames.IS_PATH, true);
        pathIntent.putExtra(ExtrasNames.PATH, path);
        sendBroadcast(pathIntent);
        Log.d(LOG_TAG, "Path sent");
    }

    private boolean isLocationValid(Location location)
    {
        int satellitesInUse = getSatellitesInUse();
        if (location.getAccuracy() < LOCATION_MIN_ACCURACY &&
                lastFix.distanceTo(location) > 7)
        {
            if(location.getProvider().equals("gps") &&  satellitesInUse < LOCATION_MIN_SATELLITES)
            {
                return false;
            }
            lastFix = location;
            Log.d(LOG_TAG,"Last fix changed to" + lastFix.getLatitude() + " " + lastFix.getLongitude());
            sendLocationBroadcast(location, satellitesInUse);
            return true;
        }
        return false;
    }
    private void checkPoints(Location location)
    {
        GeoPoint lastFixGP = new GeoPoint(this.lastFix);
        GeoPoint currentGP;

        if (nearPointNotify != null)
        {
            notificationManager.cancel(POINT_NOTIFICATION_ID);
        }

        for(Point point : points)
        {
            currentGP = new GeoPoint(point.getLatitude(), point.getLongitude());
            int distance = lastFixGP.distanceTo(currentGP);

            if( distance <= point.getRadius() + location.getAccuracy())
            {
                if(point.getClass() == WarehousePoint.class)
                {
                    showWarehouseNotify(point);
                }
                else if(((DeliveryPoint)point).getState() != TaskState.IN_WAREHOUSE)
                {
                    showTargetNotify(point);
                }
                return;
            }
        }
        hideOnPointNotify();
    }

    private void showServiceStatusNotify()
    {
        NotificationCompat.Builder serviceStatusNotify = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_service_active_notify)
                .setContentTitle("Courier Helper")
                .setContentText(getString(R.string.service_work))
                .setOngoing(true);
        notificationManager.notify(STATE_NOTIFICATION_ID, serviceStatusNotify.build());
    }
    private void showWarehouseNotify(Point point)
    {
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        Courier.getInstance().setState(CourierState.IN_WAREHOUSE);
        Log.d(LOG_TAG,"Service change courier state to " + Courier.getInstance().getState());

        resultIntent = new Intent(this, WarehouseActivity.class);
        resultIntent.putExtra(ExtrasNames.WAREHOUSE_ID, point.getID());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultPendingIntent =
                PendingIntent.getActivity(this, (int) System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        nearPointNotify = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_warehouse_notify)
                .setContentTitle("Courier Helper")
                .setContentText(getString(R.string.near_warehouse) + point.getID());
        nearPointNotify.setContentIntent(resultPendingIntent);
        notificationManager.notify(POINT_NOTIFICATION_ID, nearPointNotify.build());
    }
    private void showTargetNotify(Point point)
    {
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        Courier.getInstance().setState(CourierState.AT_THE_POINT);
        Log.d(LOG_TAG,"Service change courier state to " + Courier.getInstance().getState());

        resultIntent = new Intent(this, CompleteTaskActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(ExtrasNames.TASK_ID, point.getID());
        resultPendingIntent =
                PendingIntent.getActivity(this, (int) System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        nearPointNotify = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_task_notify)
                .setContentTitle("Courier Helper")
                .setContentText(getString(R.string.near_target) + point.getID());
        nearPointNotify.setContentIntent(resultPendingIntent);
        notificationManager.notify(POINT_NOTIFICATION_ID, nearPointNotify.build());
    }
    private void showLocationAlertNotify()
    {
        NotificationCompat.Builder locationDisabledNotify = new NotificationCompat.Builder(this)
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
        notificationManager.notify(LOCATION_ALERT_NOTIFICATION_ID, locationDisabledNotify.build());
    }

    private void hideLocationAlertNotify()
    {
        notificationManager.cancel(LOCATION_ALERT_NOTIFICATION_ID);
    }
    private void hideServiceStateNotify()
    {
        notificationManager.cancel(STATE_NOTIFICATION_ID);
    }
    private void hideOnPointNotify()
    {
        notificationManager.cancel(POINT_NOTIFICATION_ID);
    }

    private ArrayList<Road> buildOptimalPath()
    {
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(new GeoPoint(lastFix));
        for (Point point: points)
        {
            if(point instanceof DeliveryPoint && ((DeliveryPoint)point).getState() == TaskState.ON_THE_WAY)
            {
                routePoints.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
            }
        }

        ArrayList<Road> roads = null;
        PathBuilderTask tr = new PathBuilderTask();
        tr.execute(routePoints);
        try
        {
            roads = tr.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return roads;
    }
}