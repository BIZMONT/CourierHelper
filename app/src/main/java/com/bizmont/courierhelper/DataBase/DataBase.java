package com.bizmont.courierhelper.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class DataBase
{
    final static int DATA_BASE_VARSION = 1;
    DataBaseHelper dbHelper;
    SQLiteDatabase db;

    public DataBase(Context context)
    {
        dbHelper = new DataBaseHelper(context, DATA_BASE_VARSION);
    }
    public List getReports(String date)
    {
        db = dbHelper.getReadableDatabase();

        //TODO: Add code for getting delivery report to DB

        dbHelper.close();
        return null;
    }
    public void addReport()
    {
        db = dbHelper.getWritableDatabase();

        //TODO: Add code for adding delivery report to DB

        dbHelper.close();
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
        db.execSQL("create table delivery(" +
                "ID integer" +
                "");
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