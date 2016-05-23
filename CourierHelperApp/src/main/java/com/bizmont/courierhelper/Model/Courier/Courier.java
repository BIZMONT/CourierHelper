package com.bizmont.courierhelper.Model.Courier;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bizmont.courierhelper.DataBase.DatabaseManager;

public class Courier
{
    public static final String TABLE_NAME = "Couriers";

    public static final String EMAIL = "Email";
    public static final String STATE = "State";
    public static final String NAME = "Name";
    public static final String COMPLETED_TASKS = "CompletedTasks";
    public static final String SUCCESSFUL_TASKS = "SuccessfulTasks";

    private String email;

    public Courier(String email) throws IllegalArgumentException
    {
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{EMAIL}, EMAIL +" = ?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst())
        {
            this.email = cursor.getString(cursor.getColumnIndex(EMAIL));

            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
        }
        else
        {
            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
            throw new IllegalArgumentException("Can`t find courier with " + email + " email in database");
        }
    }

    public CourierState getState()
    {
        CourierState state = CourierState.NOT_ACTIVE;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{STATE}, EMAIL +" = ?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst())
        {
            state = CourierState.parse(cursor.getString(cursor.getColumnIndex(STATE)));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return state;
    }
    public void setState(CourierState state)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATE, state.toString());

        SQLiteDatabase dataBase = DatabaseManager.getInstance().openDatabase();
        dataBase.update(TABLE_NAME, contentValues, EMAIL +" = ?", new String[]{email});
        DatabaseManager.getInstance().closeDatabase();
    }

    public String getEmail()
    {
        return email;
    }

    public String getName()
    {
        String name = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{NAME}, EMAIL +" = ?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst())
        {
            name =cursor.getString(cursor.getColumnIndex(NAME));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return name;
    }

    public static void add(String email, String name)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        Cursor cursor = database.query(TABLE_NAME, new String[]{EMAIL}, EMAIL +" = ?",
                new String[]{email}, null, null, null);
        if(cursor.getCount() == 0)
        {

            contentValues.put(EMAIL, email);
            contentValues.put(NAME, name);
            contentValues.put(COMPLETED_TASKS,0);
            contentValues.put(SUCCESSFUL_TASKS, 0);
            contentValues.put(STATE, CourierState.NOT_ACTIVE.toString());

            database.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();
    }
}
