package com.bizmont.courierhelper.OtherStuff;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bizmont.courierhelper.Activities.CompleteTaskActivity;
import com.bizmont.courierhelper.Activities.WarehouseActivity;
import com.bizmont.courierhelper.Models.Point;
import com.bizmont.courierhelper.R;

public final class Notifications
{
    public static final int LOCATION_ALERT_NOTIFICATION_ID = 1;
    public static final int STATE_NOTIFICATION_ID = 2;
    public static final int POINT_NOTIFICATION_ID = 3;

    Context context;

    NotificationManager notificationManager;

    NotificationCompat.Builder nearPointNotify;
    NotificationCompat.Builder locationDisabledNotify;
    NotificationCompat.Builder serviceStatusNotify;

    boolean isPointNotifyShows;

    SharedPreferences sharedPreferences;

    public Notifications(Context context)
    {
        isPointNotifyShows = false;

        this.context = context;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nearPointNotify = new NotificationCompat.Builder(context)
                .setContentTitle("Courier Helper")
                .setOngoing(true);

        locationDisabledNotify = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_location_off_notify)
                .setContentTitle(context.getString(R.string.location_disabled))
                .setContentText(context.getString(R.string.location_disabled_message));

        serviceStatusNotify = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_service_active_notify)
                .setContentTitle("Courier Helper")
                .setContentText(context.getString(R.string.service_work))
                .setOngoing(true);
    }

    public void showServiceStatusNotify()
    {
        notificationManager.notify(STATE_NOTIFICATION_ID, serviceStatusNotify.build());
    }
    public void showWarehouseNotify(Point point)
    {
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        resultIntent = new Intent(context, WarehouseActivity.class);
        resultIntent.putExtra(ExtrasNames.WAREHOUSE_ID, point.getId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(),
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        nearPointNotify.setSmallIcon(R.drawable.ic_warehouse_notify)
                .setContentText(context.getString(R.string.near_warehouse) + point.getId())
                .setSound(Uri.parse(sharedPreferences.getString("notifications_ringtone","content://settings/system/notification_sound")));
        Log.d("Notification", "Uri " + sharedPreferences.getString("notifications_ringtone","content://settings/system/notification_sound"));
        if(sharedPreferences.getBoolean("notifications_vibrate",true))
        {
            nearPointNotify.setVibrate(new long[]{1000, 1000});
        }
        else
        {
            nearPointNotify.setVibrate(new long[]{});
        }

        nearPointNotify.setContentIntent(resultPendingIntent);
        notificationManager.notify(POINT_NOTIFICATION_ID, nearPointNotify.build());
        isPointNotifyShows = true;
    }
    public void showTargetNotify(Point point)
    {
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        resultIntent = new Intent(context, CompleteTaskActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(ExtrasNames.TASK_ID, point.getId());
        resultPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(),
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        nearPointNotify.setSmallIcon(R.drawable.ic_task_notify)
                .setContentText(context.getString(R.string.near_target) + point.getId())
                .setSound(Uri.parse(sharedPreferences.getString("notifications_ringtone","content://settings/system/notification_sound")));
        Log.d("Notification", "Uri " + sharedPreferences.getString("notifications_ringtone","content://settings/system/notification_sound"));
        if(sharedPreferences.getBoolean("notifications_vibrate",true))
        {
            nearPointNotify.setVibrate(new long[]{1000, 1000});
        }
        else
        {
            nearPointNotify.setVibrate(new long[]{});
        }
        nearPointNotify.setContentIntent(resultPendingIntent);
        notificationManager.notify(POINT_NOTIFICATION_ID, nearPointNotify.build());
        isPointNotifyShows = true;
    }
    public void showLocationAlertNotify()
    {
        Intent resultIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        locationDisabledNotify.setSound(Uri.parse(sharedPreferences.getString("notifications_ringtone","content://settings/system/notification_sound")));
        if(sharedPreferences.getBoolean("notifications_vibrate",true))
        {
            locationDisabledNotify.setVibrate(new long[]{1000, 1000});
        }
        else
        {
            locationDisabledNotify.setVibrate(new long[]{});
        }

        locationDisabledNotify.setContentIntent(resultPendingIntent);
        notificationManager.notify(LOCATION_ALERT_NOTIFICATION_ID, locationDisabledNotify.build());
    }

    public void hideLocationAlertNotify()
    {
        notificationManager.cancel(LOCATION_ALERT_NOTIFICATION_ID);

    }
    public void hideServiceStateNotify()
    {
        notificationManager.cancel(STATE_NOTIFICATION_ID);
    }
    public void hideOnPointNotify()
    {
        notificationManager.cancel(POINT_NOTIFICATION_ID);
        isPointNotifyShows = false;
    }

    public boolean isPointNotifyShows()
    {
        return isPointNotifyShows;
    }
}
