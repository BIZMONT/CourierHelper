package com.bizmont.courierhelper.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bizmont.courierhelper.Model.Courier.Courier;
import com.bizmont.courierhelper.Model.Receiver;
import com.bizmont.courierhelper.Model.Report;
import com.bizmont.courierhelper.Model.Sender;
import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.Model.Warehouse;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager
{
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager instance;
    private static DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeDatabase(Context context)
    {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = new DatabaseHelper(context);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initialize(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }
    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }
    public static synchronized void releaseDatabase()
    {
        mDatabaseHelper.close();
    }
}

class DatabaseHelper extends SQLiteOpenHelper
{
    final static int DATA_BASE_VERSION = 1;

    public DatabaseHelper(Context context)
    {
        super(context, "CourierHelperDB", null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + Task.TABLE_NAME +
                "(" +
                Task.ID + " integer PRIMARY KEY, " +
                Task.STATE + " text, " +
                Task.RECEIVER_ID + " integer, " +
                Task.SENDER_ID + " integer, " +
                Task.COURIER_EMAIL + " text, " +
                Task.ADDRESS + " text, " +
                Task.LATITUDE + " real, " +
                Task.LATITUDE + " real, " +
                Task.WAREHOUSE_ID + " integer, " +
                Task.CONTENT + " text, " +
                Task.DATE + " text, " +
                Task.COMMENT + " text, " +
                Task.CODE + " text, " +
                "FOREIGN KEY (" + Task.RECEIVER_ID + ") REFERENCES " + Receiver.TABLE_NAME + "(" + Receiver.ID + "), " +
                "FOREIGN KEY (" + Task.WAREHOUSE_ID + ") REFERENCES " + Warehouse.TABLE_NAME + "(" + Warehouse.ID + "), " +
                "FOREIGN KEY (" + Task.SENDER_ID + ") REFERENCES " + Sender.TABLE_NAME + "(" + Sender.ID + "), " +
                "FOREIGN KEY (" + Task.COURIER_EMAIL + ") REFERENCES " + Courier.TABLE_NAME + "(" + Courier.EMAIL + ")" +
                ");");
        db.execSQL("CREATE TABLE " + Warehouse.TABLE_NAME +
                "(" +
                Warehouse.ID + " integer PRIMARY KEY, " +
                Warehouse.ADDRESS + " text, " +
                Warehouse.LATITUDE + " real, " +
                Warehouse.LONGITUDE + " real, " +
                Warehouse.RADIUS +" real DEFAULT 20" +
                ");");
        db.execSQL("CREATE TABLE " + Receiver.TABLE_NAME +
                "(" +
                Receiver.ID + " integer PRIMARY KEY, " +
                Receiver.NAME + " text, " +
                Receiver.PHONE + " text" +
                ");");
        db.execSQL("CREATE TABLE " + Report.TABLE_NAME +
                "(" +
                Report.ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                Report.TASK_ID + " integer, " +
                Report.TRACK + " text, " +
                Report.RECOMMENDED_PATH + " text," +
                Report.BEGIN_TIME + " integer," +
                Report.END_TIME + " integer," +
                Report.REASON + " text," +
                "FOREIGN KEY (" + Report.TASK_ID + ") REFERENCES " + Task.TABLE_NAME + "(" + Task.ID + ")" +
                ");");
        db.execSQL("CREATE TABLE " + Sender.TABLE_NAME +
                "(" +
                Sender.ID + " integer PRIMARY KEY, " +
                Sender.NAME + " text, " +
                Sender.PHONE + " text, " +
                Sender.ADDRESS + " text" +
                ");");
        db.execSQL("CREATE TABLE " + Courier.TABLE_NAME +
                "(" +
                Courier.EMAIL + " text PRIMARY KEY, " +
                Courier.NAME + " text, " +
                Courier.COMPLETED_TASKS + " integer, " +
                Courier.SUCCESSFUL_TASKS + " integer, " +
                Courier.STATE + " text" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
