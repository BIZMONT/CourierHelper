package com.bizmont.courierhelper;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.bizmont.courierhelper.Activities.MapActivity;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.OtherStuff.CourierState;
import com.bizmont.courierhelper.Services.GPSTrackerService;

public class CourierHelper extends Application
{
    private ServiceConnection serviceConnection;
    private Intent serviceIntent;

    @Override
    public void onCreate()
    {
        super.onCreate();

        //Courier.initializeInstance("Bizmont", CourierState.NOT_ACTIVE);
        DataBase.initializeDatabase(getApplicationContext());

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        serviceIntent = new Intent(this,GPSTrackerService.class);
        startService(serviceIntent);

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        unbindService(serviceConnection);
    }
}
