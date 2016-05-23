package com.bizmont.courierhelper.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bizmont.courierhelper.DataBase.DatabaseManager;

import java.util.ArrayList;

public class Report
{
    private int id;
    public static final String TABLE_NAME = "Reports";

    public static final String ID = "ID";
    public static final String TASK_ID = "TaskID";
    public static final String TRACK = "Track";
    public static final String RECOMMENDED_PATH = "RecommendedPath";
    public static final String BEGIN_TIME = "BeginTime";
    public static final String END_TIME = "EndTime";
    public static final String REASON = "Reason";

    public Report(int id)
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
            throw new IllegalArgumentException("Can`t find report for task #" + id + " in database");
        }
    }

    public int getId() {
        return id;
    }
    public int getTaskId()
    {
        int taskId = 0;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{TASK_ID}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            taskId = cursor.getInt(cursor.getColumnIndex(TASK_ID));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return taskId;
    }

    public String getRecommendedPath() {
        String recommendedPath = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{RECOMMENDED_PATH}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            recommendedPath = cursor.getString(cursor.getColumnIndex(RECOMMENDED_PATH));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return recommendedPath;
    }

    public String getTrackPath() {
        String track = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{TRACK}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            track = cursor.getString(cursor.getColumnIndex(TRACK));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return track;
    }

    public long getBeginTime() {
        long beginTime = 0;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{BEGIN_TIME}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            beginTime = cursor.getLong(cursor.getColumnIndex(BEGIN_TIME));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return beginTime;
    }

    public long getEndTime() {
        long endTime = 0;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{END_TIME}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            endTime = cursor.getLong(cursor.getColumnIndex(END_TIME));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return endTime;
    }

    public String getReason() {
        String reason = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, new String[]{REASON}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            reason = cursor.getString(cursor.getColumnIndex(REASON));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return reason;
    }

    public static ArrayList<Report> getReportsWithDate(long fromDate, long toDate)
    {
        ArrayList<Report> reports = new ArrayList<>();
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE_NAME, null, END_TIME + " >= ? AND " + END_TIME + " <= ?",
                new String[]{String.valueOf(fromDate),String.valueOf(toDate)}, null, null, null);

        if (cursor.moveToFirst())
        {
            int IdColIndex = cursor.getColumnIndex(ID);

            do
            {
                Report report = new Report(cursor.getInt(IdColIndex));
                reports.add(report);
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();
        return reports;
    }
    public static void add(int taskId, String trackFilePath, String recommendedFilePath, long beginTime, long endTime, String reason)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        contentValues.put(TASK_ID, taskId);
        contentValues.put(TRACK, trackFilePath);
        contentValues.put(RECOMMENDED_PATH,recommendedFilePath);
        contentValues.put(BEGIN_TIME, beginTime);
        contentValues.put(END_TIME, endTime);
        contentValues.put(REASON, reason);

        database.insert(TABLE_NAME, null, contentValues);

        DatabaseManager.getInstance().closeDatabase();
    }
}
