package com.bizmont.courierhelper;


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

public class GPSTrackerService extends Service implements LocationListener {
    //constants
    public static final String BROADCAST_ACTION = "com.bizmont.courierhelper"; //filter
    private static final int LOCATION_REFRESH_RATE = 0; //seconds
    private static final float LOCATION_REFRESH_DISTANCE = 0; //meters
    private static final String LOG_TAG = "GPSTracker";

    //location fields
    private LocationManager locationManager;
    private GpsStatus gpsStatus;
    private String nmeaString;
    private int satellitesInUse;

    private boolean isTracked;
    private boolean isBound;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();

        isTracked = false;
        isBound = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    GPSTrackerService.this.nmeaString = nmea;
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
        if(!isTracked && !isBound)
        {
            stopSelf();
        }
        checkLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        checkEnabled();
    }
    @Override
    public void onProviderEnabled(String provider) {
        checkEnabled();
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
        if(location == null)
        {
            return;
        }
        if(location.getProvider().equals("gps"))
        {
            Log.d(LOG_TAG, "Getting location from GPS (" + location.getLatitude() + " " + location.getLongitude() + ")");
            satellitesInUse = getSatellitesInUse();
            if (location.getAccuracy() < 50 && satellitesInUse > 4) {
                sendLocation(location);
            }
        }
        else {
            Log.d(LOG_TAG, "Getting location from Network (" + location.getLatitude() + " " + location.getLongitude() + ")");
            if (location.getAccuracy() < 50) {
                sendLocation(location);
            }
        }
    }
    private void sendLocation(Location location)
    {
        Intent locationIntent = new Intent(BROADCAST_ACTION);
        locationIntent.putExtra("location", location);
        locationIntent.putExtra("nmea", nmeaString);
        locationIntent.putExtra("satellitesInUse", satellitesInUse);
        sendBroadcast(locationIntent);
        Log.d(LOG_TAG, "Location sent (" + location.getLatitude() + " " + location.getLongitude() + ")");
    }
}