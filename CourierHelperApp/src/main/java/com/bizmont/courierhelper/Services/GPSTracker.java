package com.bizmont.courierhelper.Services;


import android.Manifest;
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
import android.util.Log;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Point;
import com.bizmont.courierhelper.Models.Report.Report;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.Models.TaskState;
import com.bizmont.courierhelper.Models.Warehouse.Warehouse;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.OtherStuff.Notifications;
import com.bizmont.courierhelper.OtherStuff.PathBuilderTask;
import com.bizmont.courierhelper.RoadFile;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private static final String LOG_TAG = "GPS Tracker Service";

    //location fields
    private LocationManager locationManager;
    private GpsStatus gpsStatus;
    private Location lastFix;

    private boolean isLocationDisabled;

    ArrayList<Point> points;
    Polyline track;

    BroadcastReceiver broadcastReceiver;
    Notifications notifications;

    NetworkLocationIgnorer networkLocationIgnorer;

    File recommendedPathFile;
    String startTime;
    SimpleDateFormat simpleDateFormat;

    @Override
    public void onCreate() {
        super.onCreate();

        track = new Polyline(this);
        networkLocationIgnorer = new NetworkLocationIgnorer();
        recommendedPathFile = new File(getFilesDir() + "/kml","recommended_path.kml");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",getResources().getConfiguration().locale);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getBooleanExtra(ExtrasNames.IS_UPDATE_POINTS,false))
                {
                    points = DataBase.getTargetPoints();
                    isOnPoint(lastFix);
                    Log.d(LOG_TAG, "Point updated");
                    if(isOnTheWayExist())
                    {
                        startTime = simpleDateFormat.format(new Date());
                    }
                }
                if (intent.getBooleanExtra(ExtrasNames.IS_CREATE_ROUTE,false))
                {
                    ArrayList<Road> roads = buildOptimalPath();
                    RoadFile.saveRecommendedPathToFile(getApplicationContext(), recommendedPathFile, roads);

                    Log.d(LOG_TAG, "Route created");
                    sendPathBroadcast();
                }
                int completedTask = intent.getIntExtra(ExtrasNames.COMPLETE_TASK,0);
                if(completedTask != 0)
                {
                    String reason = intent.getStringExtra(ExtrasNames.REASON);
                    completeTask(completedTask, reason);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_RECEIVE_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

        //Service status notification
        notifications = new Notifications(this);
        notifications.showServiceStatusNotify();

        //Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
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

        notifications.hideLocationAlertNotify();
        notifications.hideServiceStateNotify();
        notifications.hideOnPointNotify();

        unregisterReceiver(broadcastReceiver);

        Log.d(LOG_TAG, "Service onDestroy");
        super.onDestroy();
    }

    //Service overridden methods
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(LOG_TAG, "Service onBind");
        sendLocationBroadcast(lastFix);
        isOnPoint(lastFix);
        return new Binder();
    }
    @Override
    public void onRebind(Intent intent)
    {
        Log.d(LOG_TAG, "Service onRebind");
        super.onRebind(intent);
        sendLocationBroadcast(lastFix);
        isOnPoint(lastFix);
    }

    //LocationListener overridden methods
    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"onLocationChanged");
        getLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider)
    {
        notifications.hideLocationAlertNotify();
        isLocationDisabled = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation(locationManager.getLastKnownLocation(provider));
        }
    }
    @Override
    public void onProviderDisabled(String provider) {checkProvidersAvailability();}

    //Own methods
    private void checkProvidersAvailability()
    {
        if(!isLocationDisabled) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                isLocationDisabled = true;
                notifications.showLocationAlertNotify();
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
            sendLocationBroadcast(location);
            if(isOnTheWayExist())
            {
                track.getPoints().add(new GeoPoint(location.getLatitude(),location.getLongitude()));
            }
            else
            {
                recommendedPathFile.delete();
                track = new Polyline(this);
            }
        }
    }
    private void sendLocationBroadcast(Location location)
    {
        Intent locationIntent = new Intent(BROADCAST_SEND_ACTION);

        locationIntent.putExtra(ExtrasNames.IS_LOCATION, true);
        locationIntent.putExtra(ExtrasNames.LOCATION, location);

        sendBroadcast(locationIntent);
        Log.d(LOG_TAG, "Location sent (" + location.getLatitude() + " " + location.getLongitude() + ")");
    }
    private void sendPathBroadcast()
    {
        Intent pathIntent = new Intent(BROADCAST_SEND_ACTION);
        pathIntent.putExtra(ExtrasNames.IS_PATH_UPDATE, true);

        sendBroadcast(pathIntent);
        Log.d(LOG_TAG, "Path sent");
    }

    private boolean isLocationValid(Location location)
    {
        long currentTime = System.currentTimeMillis();
        int satellitesInUse = getSatellitesInUse();

        if(networkLocationIgnorer.shouldIgnore(location.getProvider(),currentTime))
        {
            return false;
        }

        if (location.getAccuracy() < LOCATION_MIN_ACCURACY &&
                lastFix.distanceTo(location) > 7)
        {
            if(location.getProvider().equals("gps") &&  satellitesInUse < LOCATION_MIN_SATELLITES)
            {
                return false;
            }
            if(lastFix.getLatitude() != 0 && location.getSpeed() > 0.1)
            {
                return  false;
            }
            lastFix = location;
            Log.d(LOG_TAG,"Last fix changed to" + lastFix.getLatitude() + " " + lastFix.getLongitude());
            return true;
        }
        return false;
    }
    private boolean isOnPoint(Location location)
    {
        GeoPoint lastFixGP = new GeoPoint(this.lastFix);
        GeoPoint currentGP;

        for(Point point : points)
        {
            currentGP = new GeoPoint(point.getLatitude(), point.getLongitude());
            int distance = lastFixGP.distanceTo(currentGP);

            if( distance <= point.getRadius() + location.getAccuracy())
            {
                if (notifications.isPointNotifyShows())
                {
                    return true;
                }
                if(point.getClass() == Warehouse.class)
                {
                    notifications.showWarehouseNotify(point);
                }
                else if(((Task)point).getState() != TaskState.IN_WAREHOUSE)
                {
                    notifications.showTargetNotify(point);
                }
                return true;
            }
        }

        notifications.hideOnPointNotify();
        return false;
    }
    private boolean isOnTheWayExist()
    {
        for (Point point: points)
        {
            if(point instanceof Task && ((Task)point).getState() == TaskState.ON_THE_WAY)
            {
                return true;
            }
        }
        return false;
    }
    private ArrayList<Road> buildOptimalPath()
    {
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(new GeoPoint(lastFix));
        for (Point point: points)
        {
            if(point instanceof Task && ((Task)point).getState() == TaskState.ON_THE_WAY)
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
    public void completeTask(int completedTask, String reason)
    {
        if(createReport(completedTask, reason))
        {
            if(reason == null)
            {
                DataBase.setTaskState(TaskState.DELIVERED, completedTask);
            }
            else
            {
                DataBase.setTaskState(TaskState.NOT_DELIVERED, completedTask);
            }
        }
    }
    private boolean createReport(int taskId, String reason)
    {
        File recommended = new File(getFilesDir() + "/kml/recommended_paths", taskId + "_rec.kml");
        Date date = new Date();
        String endTime = simpleDateFormat.format(date);
        String trackFilePath;

        try
        {
            File trackFile = new File(getFilesDir() + "/kml/tracks", String.valueOf(taskId) + ".kml");
            trackFilePath = RoadFile.saveTrackToFile(trackFile, track);
            copyFile(recommendedPathFile,recommended);

            DataBase.addReport(new Report(0, taskId, recommended.getAbsolutePath(), trackFilePath, startTime, endTime, reason));
        }
        catch (IOException ex)
        {
            return false;
        }
        return true;
    }
    private void copyFile(File src, File dst) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}