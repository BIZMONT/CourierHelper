package com.bizmont.courierhelper.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bizmont.courierhelper.DataBase.DatabaseManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Sender
{
    public static final String ID = "ID";

    public static final String TABLE_NAME = "Senders";
    public static final String NAME = "Name";
    public static final String ADDRESS = "Address";
    public static final String PHONE = "Phone";

    private int id;

    public Sender(int id)
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

    public int getId()
    {
        return id;
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
    public String getAddress()
    {
        String address = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{ADDRESS}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            address = cursor.getString(cursor.getColumnIndex(ADDRESS));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return address;
    }

    public static void add(NodeList senders)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        for (int i = 0; i < senders.getLength(); i++)
        {
            Node sender = senders.item(i);
            if (sender.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) sender;

                Cursor cursor = database.query(Sender.TABLE_NAME, new String[]{ID}, ID + "= ?",
                        new String[]{element.getAttribute(ID)}, null, null, null);
                if(cursor.getCount() == 0) {

                    contentValues.put(ID, Integer.parseInt(element.getAttribute(ID)));
                    contentValues.put(NAME, element.getAttribute(NAME));
                    contentValues.put(PHONE, element.getAttribute(PHONE));
                    contentValues.put(ADDRESS, element.getAttribute(ADDRESS));

                    database.insert(Sender.TABLE_NAME, null, contentValues);
                }
                cursor.close();
            }
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
