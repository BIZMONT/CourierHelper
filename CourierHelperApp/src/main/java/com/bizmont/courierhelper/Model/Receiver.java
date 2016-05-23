package com.bizmont.courierhelper.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bizmont.courierhelper.DataBase.DatabaseManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Receiver
{
    public static final String TABLE_NAME = "Receivers";

    public static final String ID = "ID";
    public static final String NAME = "Name";
    public static final String PHONE = "Phone";

    private int id;

    public Receiver(int id)
    {
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{ID}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
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
            throw new IllegalArgumentException("Can`t find receiver with id" + id + "in database");
        }
    }

    public String getName()
    {
        String name = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{NAME}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            name = cursor.getString(cursor.getColumnIndex(NAME));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return name;
    }
    public String getPhone()
    {
        String phone = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{PHONE}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            phone = cursor.getString(cursor.getColumnIndex(PHONE));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return phone;
    }
    public int getId()
    {
        return id;
    }

    public static void add(NodeList receivers)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        for (int i = 0; i < receivers.getLength(); i++)
        {
            Node receiver = receivers.item(i);
            if (receiver.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) receiver;

                Cursor cursor = database.query(TABLE_NAME, new String[]{ID}, ID + " = ?",
                        new String[]{element.getAttribute(ID)}, null, null, null);
                if(cursor.getCount() == 0) {

                    contentValues.put(ID, Integer.parseInt(element.getAttribute(ID)));
                    contentValues.put(NAME, element.getAttribute(NAME));
                    contentValues.put(PHONE, element.getAttribute(PHONE));

                    database.insert(TABLE_NAME, null, contentValues);
                }
                cursor.close();
            }
        }

        DatabaseManager.getInstance().closeDatabase();
    }
}
