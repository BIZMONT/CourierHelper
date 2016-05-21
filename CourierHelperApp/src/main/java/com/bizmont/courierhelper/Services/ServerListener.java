package com.bizmont.courierhelper.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;

import com.bizmont.courierhelper.ExtrasNames;

import java.util.Timer;
import java.util.TimerTask;

public class ServerListener extends Service
{
    private long interval;

    private Timer timer;
    private TimerTask timerTask;
    private BroadcastReceiver coordinatesBroadcastReceiver;
    private BroadcastReceiver actionsBroadcastReceiver;

    Location lastFix;

    @Override
    public void onCreate()
    {
        super.onCreate();
        interval = 60000;
        timer = new Timer();

        coordinatesBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                boolean isLocation  = intent.getBooleanExtra(ExtrasNames.IS_LOCATION,false);
                if(isLocation)
                {
                    lastFix = intent.getParcelableExtra(ExtrasNames.LOCATION);
                    sendDataToServer(lastFix);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(GPSTracker.BROADCAST_SEND_ACTION);
        registerReceiver(coordinatesBroadcastReceiver, intentFilter);

        startRequestLoop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(coordinatesBroadcastReceiver);
    }

    private void sendDataToServer(Location location)
    {

    }

    void startRequestLoop()
    {
        if (timerTask != null) timerTask.cancel();
        if (interval > 0)
        {
            timerTask = new TimerTask() {
                public void run()
                {
                    checkServer();
                }
            };
            timer.schedule(timerTask, 60000, interval);
        }
    }

    private void checkServer()
    {
        //TODO:Server request for getting new tasks
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

}
