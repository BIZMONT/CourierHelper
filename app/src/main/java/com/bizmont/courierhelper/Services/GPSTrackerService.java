package com.bizmont.courierhelper.Services;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;

public class GPSTrackerService extends Service implements LocationListener {
    //constants
    public static final String BROADCAST_ACTION = "com.bizmont.courierhelper"; //filter
    public static final int LOCATION_REFRESH_RATE = 0; //seconds
    public static final float LOCATION_REFRESH_DISTANCE = 0; //meters
    public static final float LOCATION_MIN_ACCURACY = 50;
    public static final int LOCATION_MIN_SATELLITES = 4;

    private static final String LOG_TAG = "GPSTracker";

    //location fields
    private LocationManager locationManager;
    private GpsStatus gpsStatus;
    private Location lastFix;
    private String nmea;
    private int satellitesInUse;
    private ArrayList<Location> track;

    private boolean isTracked;
    private boolean isBound;

    @Override
    public void onCreate() {
        super.onCreate();

        isTracked = false;
        isBound = false;

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

        lastFix = new Location(LocationManager.GPS_PROVIDER);
        lastFix.setLatitude(0);
        lastFix.setLongitude(0);
        Log.d(LOG_TAG, "onCreate");
    }
    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    //Service overridden methods
    @Override
    public IBinder onBind(Intent arg0) {
        isBound = true;
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        isBound = false;
        return true;
    }

    //LocationListener overridden methods
    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"onLocationChanged");
        if(!isTracked && !isBound)
        {
            //stopSelf();
        }
        checkLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        checkEnabled();
    }
    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkLocation(locationManager.getLastKnownLocation(provider));
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
        checkEnabled();
    }

    //Own methods
    private void checkEnabled()
    {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            Log.d(LOG_TAG,"Location providers is disabled!");
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
    private void checkLocation(Location location)
    {
        //TODO: Make better checkLocation
        if(location == null) {
            return;
        }
        Log.d(LOG_TAG,"Location received: " + location.getLatitude() + " " + location.getLongitude());
        if(location.getProvider().equals("gps"))
        {
            Log.d(LOG_TAG, "Getting location from GPS (" + location.getLatitude() + " " +
                    location.getLongitude() + ")");

            satellitesInUse = getSatellitesInUse();
            if (location.getAccuracy() < LOCATION_MIN_ACCURACY &&
                    satellitesInUse > LOCATION_MIN_SATELLITES &&
                    lastFix.distanceTo(location) > 7) {
                lastFix = location;
                sendLocation(location);
            }
        }
        else {
            Log.d(LOG_TAG, "Getting location from Network (" + location.getLatitude() + " " +
                    location.getLongitude() + ")");

            if (location.getAccuracy() < LOCATION_MIN_ACCURACY &&
                    lastFix.distanceTo(location) > 7 ) {
                lastFix = location;
                sendLocation(location);
            }
        }
    }
    private void sendLocation(Location location)
    {
        Intent locationIntent = new Intent(BROADCAST_ACTION);

        locationIntent.putExtra("location", location);
        locationIntent.putExtra("nmea", nmea);
        locationIntent.putExtra("satellitesInUse", satellitesInUse);

        sendBroadcast(locationIntent);
        Log.d(LOG_TAG, "Location sent (" + location.getLatitude() + " " + location.getLongitude() + ")");
    }
}