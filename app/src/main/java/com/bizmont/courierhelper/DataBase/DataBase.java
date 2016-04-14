package com.bizmont.courierhelper.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bizmont.courierhelper.OtherStuff.TaskState;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public final class DataBase
{
    static final String LOG_TAG = "DBLog";

    public static void initializeDatabase(Context context)
    {
        DatabaseManager.initializeDatabase(new DataBaseHelper(context));
    }

    public static void WriteData(File file)
    {
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();

            NodeList table = document.getElementsByTagName("Task");

            PutTasksToDB(table);
            Log.d(LOG_TAG,"Tasks added!");

            table = document.getElementsByTagName("Warehouse");
            PutWarehousesToDB(table);
            Log.d(LOG_TAG,"Warehouses added!");

            table = document.getElementsByTagName("Recipient");
            PutRecipientsToDB(table);
            Log.d(LOG_TAG,"Recipients added!");
        }
        catch (Exception e)
        {
            Log.d(LOG_TAG,"Error in parsing file: " + e.getMessage());
        }
    }
    public static void ChangeTaskStatus(TaskState state, int DeliveryId)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("State",state.toString());

        SQLiteDatabase dataBase = DatabaseManager.getInstance().openDatabase();
        dataBase.update("Tasks",contentValues,"id=?",new String[]{Integer.toString(DeliveryId)});
        DatabaseManager.getInstance().closeDatabase();
    }
    public static void GetActiveTasks()
    {
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();



        DatabaseManager.getInstance().closeDatabase();
    }

    //TODO:Check if row is already existing
    private static void PutRecipientsToDB(NodeList recipients)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < recipients.getLength(); i++)
        {
            Node recipient = recipients.item(i);
            if (recipient.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) recipient;

                contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                contentValues.put("Name", element.getAttribute("Name"));
                contentValues.put("Phone", element.getAttribute("Phone"));

                SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
                database.insert("Recipients", null, contentValues);
                DatabaseManager.getInstance().closeDatabase();
            }
        }
    }
    private static void PutWarehousesToDB(NodeList warehouses)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < warehouses.getLength(); i++)
        {
            Node warehouse = warehouses.item(i);
            if (warehouse.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) warehouse;

                contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                contentValues.put("Address", element.getAttribute("Address"));
                contentValues.put("Latitude", Float.parseFloat(element.getAttribute("Latitude")));
                contentValues.put("Longitude", Float.parseFloat(element.getAttribute("Longitude")));
                contentValues.put("Radius", Float.parseFloat(element.getAttribute("Radius")));

                SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
                database.insert("Warehouses", null, contentValues);
                DatabaseManager.getInstance().closeDatabase();
            }
        }
    }
    private static void PutTasksToDB(NodeList table)
    {
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < table.getLength(); i++)
        {
            Node task = table.item(i);

            if (task.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) task;

                Log.d(LOG_TAG,element.getAttribute("ID"));

                contentValues.put("ID", Integer.parseInt(element.getAttribute("ID")));
                contentValues.put("Content", element.getAttribute("Content"));
                contentValues.put("RecipientID", Integer.parseInt(element.getAttribute("RecipientID")));
                contentValues.put("Address", element.getAttribute("Address"));
                contentValues.put("Latitude", Float.parseFloat(element.getAttribute("Latitude")));
                contentValues.put("Longitude", Float.parseFloat(element.getAttribute("Longitude")));
                contentValues.put("State", TaskState.IN_WAREHOUSE.toString());
                contentValues.put("WarehouseID", Integer.parseInt(element.getAttribute("WarehouseID")));
                contentValues.put("Date", element.getAttribute("Date"));
                contentValues.put("Comment", element.getAttribute("Comment"));

                SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
                database.insert("Tasks", null, contentValues);
                DatabaseManager.getInstance().closeDatabase();
            }
        }
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
                    " is not initialized, call initializeDatabase(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }
    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
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
            db.execSQL("CREATE TABLE Tasks" +
                    "(" +
                    "ID integer PRIMARY KEY, " +
                    "Content text, " +
                    "RecipientID integer, " +
                    "Address text, " +
                    "Latitude real, " +
                    "Longitude real, " +
                    "State text, " +
                    "WarehouseID integer, " +
                    "Date text, " +
                    "Comment text, " +
                    "FOREIGN KEY (RecipientID) REFERENCES Recipients(ID), " +
                    "FOREIGN KEY (WarehouseID) REFERENCES Warehouse(ID)" +
                    ");");
            db.execSQL("CREATE TABLE Warehouses(" +
                    "ID integer PRIMARY KEY, " +
                    "Address text, " +
                    "Latitude real, " +
                    "Longitude real, " +
                    "Radius real DEFAULT 5" +
                    ");");
            db.execSQL("CREATE TABLE Recipients(" +
                    "ID integer PRIMARY KEY, " +
                    "Name text, " +
                    "Phone text" +
                    ");");
            db.execSQL("CREATE TABLE Reports(" +
                    "ID integer PRIMARY KEY AUTOINCREMENT, " +
                    "TaskID integer, " +
                    "Track text DEFAULT 'No track'," +
                    "BeginTime text," +
                    "EndTime text," +
                    "FOREIGN KEY (TaskID) REFERENCES Tasks(ID)" +
                    ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}