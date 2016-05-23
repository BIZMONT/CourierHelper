package com.bizmont.courierhelper.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bizmont.courierhelper.DataBase.DatabaseManager;
import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.Model.Task.TaskState;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class Point
{
    protected int id;
    protected String TABLE;

    public static final String ID = "ID";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String ADDRESS = "Address";
    public static final String RADIUS = "Radius";

    public int getId(){return id;}

    public double getLatitude()
    {
        double latitude = 0;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{LATITUDE}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            latitude = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return latitude;
    }
    public double getLongitude()
    {
        double longitude = 0;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{LONGITUDE}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            longitude = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return longitude;
    }
    public String getAddress() {
        String address = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{ADDRESS}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            address = cursor.getString(cursor.getColumnIndex(ADDRESS));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return address;
    }
    public abstract float getRadius();

    public abstract Marker createMarker(Context context, MapView map);

    public static ArrayList<Point> getTargetPoints(String courierEmail)
    {
        ArrayList<Point> points = new ArrayList<>();
        HashSet<Integer> warehouses = new HashSet<>();

        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        String sqlCommand = "SELECT " +
                Task.TABLE_NAME + "."+ ID + " AS TaskID, " +
                Warehouse.TABLE_NAME + "." + ID + " AS WarehouseID " +
                " FROM " + Task.TABLE_NAME +
                " LEFT OUTER JOIN " + Warehouse.TABLE_NAME + " ON " + Task.TABLE_NAME + ".WarehouseID = " + Warehouse.TABLE_NAME + "." + ID +
                " WHERE CourierEmail = \"" + courierEmail + "\"  AND " +
                "(" + Task.TABLE_NAME + "." + Task.STATE + " = \"" + TaskState.ON_THE_WAY + "\" OR " + Task.TABLE_NAME + "." + Task.STATE + " = \"" + TaskState.IN_WAREHOUSE +"\");";

        cursor = database.rawQuery(sqlCommand,null);

        if (cursor.moveToFirst())
        {
            int taskIDColIndex = cursor.getColumnIndex("TaskID");

            int warehouseIDColIndex = cursor.getColumnIndex("WarehouseID");

            do
            {
                points.add(new Task(cursor.getInt(taskIDColIndex)));

                int warehouseID = cursor.getInt(warehouseIDColIndex);
                if(!warehouses.contains(warehouseID))
                {
                    Warehouse warehouse = new Warehouse(warehouseID);
                    warehouses.add(warehouseID);
                    points.add(warehouse);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();
        return points;
    }
}

