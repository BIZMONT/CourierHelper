package com.bizmont.courierhelper.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class ServerListener extends Service
{
    private long interval;

    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onCreate()
    {
        super.onCreate();
        interval = 60000;
        timer = new Timer();
        schedule();
    }

    void schedule()
    {
        if (timerTask != null) timerTask.cancel();
        if (interval > 0) {
            timerTask = new TimerTask() {
                public void run()
                {

                }
            };
            timer.schedule(timerTask, 1000, interval);
        }
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
