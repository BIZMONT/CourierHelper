package com.bizmont.courierhelper;

import android.app.Application;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.OtherStuff.CourierState;

public class CourierHelper extends Application
{
    @Override
    public void onCreate()
    {
        Courier.initializeInstance("Bizmont", CourierState.NOT_ACTIVE);
        super.onCreate();

        DataBase.initializeDatabase(getApplicationContext());

    }
}
