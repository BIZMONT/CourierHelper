package com.bizmont.courierhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase
{
    int DATA_BASE_VERSION = 1;
    public void GetPOI()
    {

    }
}

class DataBaseHelper extends SQLiteOpenHelper
{
    public DataBaseHelper(Context context, int dbVersion)
    {
        super(context, "CourierHelperDB", null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table orders (" +
                "ID integer, " +
                "Name text, " +
                "RecipientName text, " +
                "Count integer, " +
                "Address text, " +
                "Status text," +
                "AddressLatitude real, " +
                "AddressLongitude real, " +
                "WarehouseLatitude real, " +
                "WarehouseLongitude real, " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}