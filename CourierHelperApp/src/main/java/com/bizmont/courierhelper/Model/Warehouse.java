package com.bizmont.courierhelper.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;

import com.bizmont.courierhelper.DataBase.DatabaseManager;
import com.bizmont.courierhelper.R;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Warehouse extends Point
{
    public static final String TABLE_NAME = "Warehouses";

    public Warehouse(int id)
    {
        TABLE = TABLE_NAME;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{ID}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            this.id = cursor.getInt(cursor.getColumnIndex(ID));
            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
        }
        else
        {
            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
            throw new IllegalArgumentException("Can`t find warehouse #" + id + "in database");
        }
    }

    @Override
    public float getRadius() {
        float radius = 0;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{RADIUS}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            radius = cursor.getFloat(cursor.getColumnIndex(RADIUS));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return radius;
    }

    @Override
    public Marker createMarker(Context context, MapView map) {
        Marker marker = new Marker(map);
        marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_warehouse));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setPosition(new GeoPoint(getLatitude(),getLongitude()));
        marker.setTitle(getAddress());
        return marker;
    }

    public static void add(NodeList warehouses)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        for (int i = 0; i < warehouses.getLength(); i++)
        {
            Node warehouse = warehouses.item(i);
            if (warehouse.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) warehouse;

                Cursor cursor = database.query(TABLE_NAME, new String[]{ID}, ID + " = ?",
                        new String[]{element.getAttribute(ID)}, null, null, null);
                if(cursor.getCount() == 0) {
                    contentValues.put(ID, Integer.parseInt(element.getAttribute(ID)));
                    contentValues.put(ADDRESS, element.getAttribute(ADDRESS));
                    contentValues.put(LATITUDE, Float.parseFloat(element.getAttribute(LATITUDE)));
                    contentValues.put(LONGITUDE, Float.parseFloat(element.getAttribute(LONGITUDE)));
                    contentValues.put(RADIUS, Float.parseFloat(element.getAttribute(RADIUS)));

                    database.insert(TABLE_NAME, null, contentValues);
                }
                cursor.close();
            }
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
