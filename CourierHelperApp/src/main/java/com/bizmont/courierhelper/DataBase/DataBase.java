package com.bizmont.courierhelper.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bizmont.courierhelper.Models.Report.Report;
import com.bizmont.courierhelper.Models.Report.ReportDetails;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.Models.Task.TaskDetails;
import com.bizmont.courierhelper.Models.Task.TaskState;
import com.bizmont.courierhelper.Point.DeliveryPoint;
import com.bizmont.courierhelper.Point.Point;
import com.bizmont.courierhelper.Point.WarehousePoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public final class DataBase
{
    static final String LOG_TAG = "Data Base";

    public static void initialize(Context context)
    {
        DatabaseManager.initializeDatabase(new DataBaseHelper(context));
        Log.d(LOG_TAG, "Initialized");
    }
    public static void close()
    {
        DatabaseManager.releaseDatabase();
    }
    public static void addData(File file)
    {
        //TODO: catch parsing exceptions
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();

            NodeList table = document.getElementsByTagName("Task");

            SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

            PutTasksToDB(table, database);
            Log.d(LOG_TAG,"Tasks added!");

            table = document.getElementsByTagName("Warehouse");
            PutWarehousesToDB(table, database);
            Log.d(LOG_TAG,"Warehouses added!");

            table = document.getElementsByTagName("Receiver");
            PutReceiversToDB(table, database);
            Log.d(LOG_TAG,"Receivers added!");

            table = document.getElementsByTagName("Sender");
            PutSendersToDB(table, database);
            Log.d(LOG_TAG,"Senders added!");

            DatabaseManager.getInstance().closeDatabase();
        }
        catch (Exception e)
        {
            Log.d(LOG_TAG,"Error in parsing file: " + e.getMessage());
        }
    }
    public static void setTaskState(TaskState state, int taskId)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("State",state.toString());

        SQLiteDatabase dataBase = DatabaseManager.getInstance().openDatabase();
        dataBase.update(Tables.TASKS, contentValues, "id=?", new String[]{Integer.toString(taskId)});
        DatabaseManager.getInstance().closeDatabase();
    }
    public static ArrayList<Point> getTargetPoints()
    {
        ArrayList<Point> points = new ArrayList<>();
        HashSet<Integer> warehouses = new HashSet<>();

        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        String sqlCommand = "SELECT " +
                Tables.TASKS + ".ID AS TaskID, " +
                Tables.TASKS + ".State, " +
                Tables.TASKS + ".Address AS TaskAddress, " +
                Tables.TASKS + ".Latitude AS TaskLat, " +
                Tables.TASKS + ".Longitude AS TaskLong, " +
                Tables.WAREHOUSES + ".ID AS WarehouseID, " +
                Tables.WAREHOUSES + ".Address AS WarehouseAddress, " +
                Tables.WAREHOUSES + ".Latitude AS WarehouseLat, " +
                Tables.WAREHOUSES + ".Longitude AS WarehouseLong, " +
                Tables.WAREHOUSES + ".Radius " +
                "FROM " + Tables.TASKS +
                " LEFT OUTER JOIN " + Tables.WAREHOUSES + " ON " + Tables.TASKS + ".WarehouseID = " + Tables.WAREHOUSES + ".ID" +
                " WHERE " + Tables.TASKS + ".State = \"" + TaskState.ON_THE_WAY + "\" OR " + Tables.TASKS + ".State = \"" + TaskState.IN_WAREHOUSE +"\";";

        cursor = database.rawQuery(sqlCommand,null);

        if (cursor.moveToFirst())
        {
            int taskIDColIndex = cursor.getColumnIndex("TaskID");
            int taskAddressColIndex = cursor.getColumnIndex("TaskAddress");
            int stateColIndex = cursor.getColumnIndex("State");
            int taskLatColIndex = cursor.getColumnIndex("TaskLat");
            int taskLongColIndex = cursor.getColumnIndex("TaskLong");

            int warehouseIDColIndex = cursor.getColumnIndex("WarehouseID");
            int warehouseAddressColIndex = cursor.getColumnIndex("WarehouseAddress");
            int warehouseLatColIndex = cursor.getColumnIndex("WarehouseLat");
            int warehouseLongColIndex = cursor.getColumnIndex("WarehouseLong");
            int warehouseRadiusColIndex = cursor.getColumnIndex("Radius");

            do
            {
                points.add(new DeliveryPoint(
                        cursor.getString(taskAddressColIndex),
                        cursor.getDouble(taskLatColIndex),
                        cursor.getDouble(taskLongColIndex),
                        TaskState.Parse(cursor.getString(stateColIndex)),
                        cursor.getInt(taskIDColIndex),
                        cursor.getDouble(warehouseLatColIndex),
                        cursor.getDouble(warehouseLongColIndex)
                ));

                int warehouseID = cursor.getInt(warehouseIDColIndex);
                if(!warehouses.contains(warehouseID))
                {
                    WarehousePoint warehousePoint = new WarehousePoint(
                            cursor.getString(warehouseAddressColIndex),
                            cursor.getDouble(warehouseLatColIndex),
                            cursor.getDouble(warehouseLongColIndex),
                            cursor.getFloat(warehouseRadiusColIndex),
                            warehouseID
                    );
                    warehouses.add(warehouseID);
                    points.add(warehousePoint);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();
        return points;
    }
    public static Task[] getActiveTasks(int warehouseId)
    {
        Task[] tasks;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        if(warehouseId == 0)
        {
            cursor = database.query(Tables.TASKS, null, "State != ? OR State != ?",
                    new String[]{TaskState.DELIVERED.toString(),TaskState.NOT_DELIVERED.toString()},
                    null, null, null);
        }
        else
        {
            cursor = database.query(Tables.TASKS, null, "(State = ? OR State = ?) AND WarehouseID = ?",
                    new String[]{TaskState.IN_WAREHOUSE.toString(), TaskState.ON_THE_WAY.toString(), Integer.toString(warehouseId)},
                    null, null, null);
        }

        tasks = new Task[cursor.getCount()];

        if (cursor.moveToFirst())
        {
            int idColIndex = cursor.getColumnIndex("ID");
            int addressColIndex = cursor.getColumnIndex("Address");
            int stateColIndex = cursor.getColumnIndex("State");

            int i = 0;
            do
            {
                tasks[i] = new Task(cursor.getInt(idColIndex),
                        TaskState.Parse(cursor.getString(stateColIndex)),
                        cursor.getString(addressColIndex));
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();
        return tasks;
    }
    public static TaskDetails getTaskDetails(int taskId)
    {
        TaskDetails taskDetails = null;

        String sqlCommand = "SELECT " +
                Tables.TASKS + ".State, " +
                Tables.TASKS + ".Address AS ReceiverAddress, " +
                Tables.TASKS + ".Content, " +
                Tables.TASKS + ".Date, " +
                Tables.TASKS + ".Comment, " +
                Tables.RECEIVERS + ".Name, " +
                Tables.RECEIVERS + ".Phone, " +
                Tables.SENDERS + ".Address AS SenderAddress, " +
                Tables.SENDERS + ".Name AS SenderName, " +
                Tables.SENDERS + ".Phone AS SenderPhone, " +
                Tables.WAREHOUSES + ".Address AS WarehouseAddress " +
                "FROM " + Tables.TASKS +
                " LEFT OUTER JOIN " + Tables.RECEIVERS + " ON " + Tables.TASKS + ".ReceiverID = " + Tables.RECEIVERS + ".ID " +
                "LEFT OUTER JOIN " + Tables.WAREHOUSES + " ON " + Tables.TASKS + ".WarehouseID = " + Tables.WAREHOUSES + ".ID " +
                "LEFT OUTER JOIN " + Tables.SENDERS + " ON " + Tables.TASKS + ".SenderID = " + Tables.SENDERS + ".ID " +
                "WHERE " + Tables.TASKS + ".ID = ?;";

        Cursor cursor;
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        cursor = database.rawQuery(sqlCommand,new String[]{Integer.toString(taskId)});

        if (cursor.moveToFirst())
        {
            do
            {
                taskDetails = new TaskDetails(
                        taskId,
                        TaskState.Parse(cursor.getString(cursor.getColumnIndex("State"))),
                        cursor.getString(cursor.getColumnIndex("ReceiverAddress")),
                        cursor.getString(cursor.getColumnIndex("Content")),
                        cursor.getString(cursor.getColumnIndex("Name")),
                        cursor.getString(cursor.getColumnIndex("Phone")),
                        cursor.getString(cursor.getColumnIndex("WarehouseAddress")),
                        cursor.getString(cursor.getColumnIndex("Date")),
                        cursor.getString(cursor.getColumnIndex("Comment")),
                        cursor.getString(cursor.getColumnIndex("SenderName")),
                        cursor.getString(cursor.getColumnIndex("SenderAddress")),
                        cursor.getString(cursor.getColumnIndex("SenderPhone"))
                        );
            } while (cursor.moveToNext());
        }

        cursor.close();
        DatabaseManager.getInstance().closeDatabase();

        return taskDetails;
    }
    public static String getTaskCode(int taskId)
    {
        Cursor cursor;
        String result;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        cursor = database.query(Tables.TASKS, null, "ID = ?",
                new String[]{String.valueOf(taskId)}, null, null, null);

        if(cursor.moveToFirst())
        {
            result = cursor.getString(cursor.getColumnIndex("Code"));
        }
        else
        {
            throw new RuntimeException();
        }
        DatabaseManager.getInstance().closeDatabase();

        return result;
    }

    private static void PutReceiversToDB(NodeList receivers, SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < receivers.getLength(); i++)
        {
            Node receiver = receivers.item(i);
            if (receiver.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) receiver;

                Cursor cursor = database.query(Tables.RECEIVERS, new String[]{"ID"}, "ID=?",
                        new String[]{element.getAttribute("ID")}, null, null, null);
                if(cursor.getCount() == 0) {

                    contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                    contentValues.put("Name", element.getAttribute("Name"));
                    contentValues.put("Phone", element.getAttribute("Phone"));

                    database.insert(Tables.RECEIVERS, null, contentValues);
                }
                cursor.close();
            }
        }
    }
    private static void PutWarehousesToDB(NodeList warehouses, SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < warehouses.getLength(); i++)
        {
            Node warehouse = warehouses.item(i);
            if (warehouse.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) warehouse;

                Cursor cursor = database.query(Tables.WAREHOUSES, new String[]{"ID"}, "ID=?",
                        new String[]{element.getAttribute("ID")}, null, null, null);
                if(cursor.getCount() == 0) {
                    contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                    contentValues.put("Address", element.getAttribute("Address"));
                    contentValues.put("Latitude", Float.parseFloat(element.getAttribute("Latitude")));
                    contentValues.put("Longitude", Float.parseFloat(element.getAttribute("Longitude")));
                    contentValues.put("Radius", Float.parseFloat(element.getAttribute("Radius")));

                    database.insert(Tables.WAREHOUSES, null, contentValues);
                }
                cursor.close();
            }
        }
    }
    private static void PutTasksToDB(NodeList tasks, SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < tasks.getLength(); i++)
        {
            Node task = tasks.item(i);

            if (task.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) task;

                Cursor cursor = database.query(Tables.TASKS, new String[]{"ID"}, "ID=?",
                        new String[]{element.getAttribute("ID")}, null, null, null);
                if(cursor.getCount() == 0)
                {
                    contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                    contentValues.put("State", TaskState.IN_WAREHOUSE.toString());
                    contentValues.put("Content", element.getAttribute("Content"));
                    contentValues.put("ReceiverID", Integer.parseInt(element.getAttribute("ReceiverID")));
                    contentValues.put("SenderID", Integer.parseInt(element.getAttribute("SenderID")));
                    contentValues.put("Address", element.getAttribute("Address"));
                    contentValues.put("Latitude", Float.parseFloat(element.getAttribute("Latitude")));
                    contentValues.put("Longitude", Float.parseFloat(element.getAttribute("Longitude")));
                    contentValues.put("WarehouseID", Integer.parseInt(element.getAttribute("WarehouseID")));
                    contentValues.put("Date", element.getAttribute("Date"));
                    contentValues.put("Comment", element.getAttribute("Comment"));
                    contentValues.put("Code", element.getAttribute("Code"));

                    database.insert(Tables.TASKS, null, contentValues);
                }
                cursor.close();
            }
        }
    }
    private static void PutSendersToDB(NodeList senders, SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < senders.getLength(); i++)
        {
            Node sender = senders.item(i);
            if (sender.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) sender;

                Cursor cursor = database.query(Tables.SENDERS, new String[]{"ID"}, "ID=?",
                        new String[]{element.getAttribute("ID")}, null, null, null);
                if(cursor.getCount() == 0) {

                    contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                    contentValues.put("Name", element.getAttribute("Name"));
                    contentValues.put("Phone", element.getAttribute("Phone"));
                    contentValues.put("Address", element.getAttribute("Address"));

                    database.insert(Tables.SENDERS, null, contentValues);
                }
                cursor.close();
            }
        }
    }

    public static void addReport(Report report)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        contentValues.put("TaskID", report.getTaskId());
        contentValues.put("Track", report.getTrackPath());
        contentValues.put("RecommendedPath",report.getRecommendedPath());
        contentValues.put("BeginTime", report.getStarTime());
        contentValues.put("EndTime", report.getEndTime());
        contentValues.put("Reason", report.getReason());

        database.insert(Tables.REPORTS, null, contentValues);

        DatabaseManager.getInstance().closeDatabase();
    }

    public static ArrayList<Report> getReportsWithDate(String date)
    {
        ArrayList<Report> reports = new ArrayList<>();
        date = date.substring(0,9);
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(Tables.REPORTS, null, "EndTime = ?", new String[]{date}, null, null, null);

        if (cursor.moveToFirst())
        {
            int IdColIndex = cursor.getColumnIndex("ID");
            int taskIdColIndex = cursor.getColumnIndex("TaskID");
            int trackColIndex = cursor.getColumnIndex("Track");
            int pathColIndex = cursor.getColumnIndex("RecommendedPath");
            int beginColIndex = cursor.getColumnIndex("BeginTime");
            int endColIndex = cursor.getColumnIndex("EndTime");
            int reasonColIndex = cursor.getColumnIndex("Reason");

            do
            {
                Report report = new Report(
                        cursor.getInt(IdColIndex),
                        cursor.getInt(taskIdColIndex),
                        cursor.getString(pathColIndex),
                        cursor.getString(trackColIndex),
                        cursor.getString(beginColIndex),
                        cursor.getString(endColIndex),
                        cursor.getString(reasonColIndex));
                reports.add(report);
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();

        return reports;
    }

    public static ReportDetails getReportDetails(int id)
    {
        Cursor cursor;
        ReportDetails reportDetails = null;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(Tables.REPORTS, null, "ID = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst())
        {
            int IdColIndex = cursor.getColumnIndex("ID");
            int taskIdColIndex = cursor.getColumnIndex("TaskID");
            int trackColIndex = cursor.getColumnIndex("Track");
            int pathColIndex = cursor.getColumnIndex("RecommendedPath");
            int beginColIndex = cursor.getColumnIndex("BeginTime");
            int endColIndex = cursor.getColumnIndex("EndTime");
            int reasonColIndex = cursor.getColumnIndex("Reason");

            do
            {
                reportDetails = new ReportDetails(
                        cursor.getInt(IdColIndex),
                        cursor.getInt(taskIdColIndex),
                        cursor.getString(pathColIndex),
                        cursor.getString(trackColIndex),
                        cursor.getString(beginColIndex),
                        cursor.getString(endColIndex),
                        cursor.getString(reasonColIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();

        return reportDetails;
    }
}

class DatabaseManager
{
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager instance;
    private static DataBaseHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeDatabase(DataBaseHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
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

class DataBaseHelper extends SQLiteOpenHelper
{
    final static int DATA_BASE_VERSION = 1;

    public DataBaseHelper(Context context)
    {
        super(context, "CourierHelperDB", null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + Tables.TASKS +
                "(" +
                "ID integer PRIMARY KEY, " +
                "State text, " +
                "Content text, " +
                "ReceiverID integer, " +
                "SenderID integer, " +
                "Address text, " +
                "Latitude real, " +
                "Longitude real, " +
                "WarehouseID integer, " +
                "Date text, " +
                "Comment text, " +
                "Code text, " +
                "FOREIGN KEY (ReceiverID) REFERENCES " + Tables.RECEIVERS + "(ID), " +
                "FOREIGN KEY (WarehouseID) REFERENCES " + Tables.WAREHOUSES + "(ID)," +
                "FOREIGN KEY (SenderID) REFERENCES " + Tables.SENDERS + "(ID)" +
                ");");
        db.execSQL("CREATE TABLE " + Tables.WAREHOUSES +
                "(" +
                "ID integer PRIMARY KEY, " +
                "Address text, " +
                "Latitude real, " +
                "Longitude real, " +
                "Radius real DEFAULT 10" +
                ");");
        db.execSQL("CREATE TABLE " + Tables.RECEIVERS +
                "(" +
                "ID integer PRIMARY KEY, " +
                "Name text, " +
                "Phone text" +
                ");");
        db.execSQL("CREATE TABLE " + Tables.REPORTS +
                "(" +
                "ID integer PRIMARY KEY AUTOINCREMENT, " +
                "TaskID integer, " +
                "Track text" +
                "RecommendedPath text," +
                "BeginTime text," +
                "EndTime text," +
                "Reason text," +
                "FOREIGN KEY (TaskID) REFERENCES " + Tables.TASKS + "(ID)" +
                ");");
        db.execSQL("CREATE TABLE " + Tables.SENDERS +
                "(" +
                "ID integer PRIMARY KEY," +
                "Name text, " +
                "Phone text, " +
                "Address text" +
                ");");
        db.execSQL("CREATE TABLE " + Tables.COURIERS +"" +
                "(" +
                "Email text PRIMARY KEY," +
                "Username text," +
                "CompletedTasks integer," +
                "SuccessfulTasks integer" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}

final class Tables
{
    public static final String RECEIVERS = "Receivers";
    public static final String TASKS = "Tasks";
    public static final String WAREHOUSES = "Warehouses";
    public static final String REPORTS = "Reports";
    public static final String SENDERS = "Senders";
    public static final String COURIERS = "Couriers";
}