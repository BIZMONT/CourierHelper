package com.bizmont.courierhelper.Model.Task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;

import com.bizmont.courierhelper.DataBase.DatabaseManager;
import com.bizmont.courierhelper.Model.Courier.Courier;
import com.bizmont.courierhelper.Model.Point;
import com.bizmont.courierhelper.Model.Receiver;
import com.bizmont.courierhelper.Model.Sender;
import com.bizmont.courierhelper.Model.Warehouse;
import com.bizmont.courierhelper.R;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Task extends Point
{
    public static final String TABLE_NAME = "Tasks";

    public static final String STATE = "State";
    public static final String WAREHOUSE_ID = "WarehouseID";
    public static final String SENDER_ID = "SenderID";
    public static final String RECEIVER_ID = "ReceiverID";
    public static final String COURIER_EMAIL = "CourierEmail";
    public static final String CONTENT = "Content";
    public static final String COMMENT = "Comment";
    public static final String DATE = "Date";
    public static final String CODE = "Code";

    private Warehouse warehouse;
    private Receiver receiver;
    private Sender sender;
    private Courier courier;

    public Task(int id) throws IllegalArgumentException
    {
        TABLE = TABLE_NAME;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{ID, WAREHOUSE_ID, SENDER_ID, RECEIVER_ID, COURIER_EMAIL}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            this.id = cursor.getInt(cursor.getColumnIndex(ID));
            this.warehouse = new Warehouse(cursor.getInt(cursor.getColumnIndex(WAREHOUSE_ID)));
            this.sender = new Sender(cursor.getInt(cursor.getColumnIndex(SENDER_ID)));
            this.receiver = new Receiver(cursor.getInt(cursor.getColumnIndex(RECEIVER_ID)));
            this.courier = new Courier(cursor.getString(cursor.getColumnIndex(COURIER_EMAIL)));

            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
        }
        else
        {
            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
            throw new IllegalArgumentException("Can`t find task " + id + "in database");
        }
    }

    @Override
    public float getRadius()
    {
        return 20;
    }

    public TaskState getState()
    {
        TaskState state = TaskState.IN_WAREHOUSE;
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{STATE}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            state = TaskState.Parse(cursor.getString(cursor.getColumnIndex(STATE)));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return state;
    }
    public void setState(TaskState state)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATE, state.toString());

        SQLiteDatabase dataBase = DatabaseManager.getInstance().openDatabase();
        dataBase.update(TABLE, contentValues, ID + " = ?", new String[]{Integer.toString(id)});
        DatabaseManager.getInstance().closeDatabase();
    }
    public Warehouse getWarehouse()
    {
        return warehouse;
    }

    public Sender getSender() {
        return sender;
    }
    public Receiver getReceiver() {
        return receiver;
    }
    public Courier getCourier() {
        return courier;
    }

    public String getContent()
    {
        String content = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{CONTENT}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            content = cursor.getString(cursor.getColumnIndex(CONTENT));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return content;
    }
    public String getComment()
    {
        String comment = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{COMMENT}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            comment = cursor.getString(cursor.getColumnIndex(COMMENT));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return comment;
    }
    public String getDate()
    {
        String date = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{DATE}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            date = cursor.getString(cursor.getColumnIndex(DATE));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return date;
    }
    public String getCode()
    {
        String code = "Unknown";
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        cursor = database.query(TABLE, new String[]{CODE}, ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst())
        {
            code = cursor.getString(cursor.getColumnIndex(CODE));
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return code;
    }

    @Override
    public Marker createMarker(Context context, final MapView map)
    {
        Marker marker = new Marker(map);
        switch (getState())
        {
            case DELIVERED:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_green));
                break;
            case NOT_DELIVERED:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_red));
                break;
            case IN_WAREHOUSE:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_yellow));
                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        Warehouse warehouse = getWarehouse();
                        map.getController().animateTo(new GeoPoint(warehouse.getLatitude(), warehouse.getLongitude()));
                        return true;
                    }
                });
                break;
            case ON_THE_WAY:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_blue));
                break;
        }

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setPosition(new GeoPoint(getLatitude(),getLongitude()));
        marker.setTitle(context.getString(R.string.task_number) + Integer.toString(id));
        marker.setSubDescription(context.getString(R.string.address) + ": " + getAddress());
        return marker;
    }

    public static ArrayList<Task> getActiveTasks(int warehouseId, String courierEmail)
    {
        ArrayList<Task> tasks = new ArrayList<>();
        Cursor cursor;

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        if(warehouseId == 0)
        {
            cursor = database.query(TABLE_NAME, null, "(" + STATE + " = ? OR " + STATE + " = ?) AND " + COURIER_EMAIL + " = ?",
                    new String[]{TaskState.ON_THE_WAY.toString(), TaskState.IN_WAREHOUSE.toString(), courierEmail},
                    null, null, null);
        }
        else
        {
            cursor = database.query(TABLE_NAME, null, "(" + STATE + " = ? OR " + STATE + " = ?) AND " + WAREHOUSE_ID + " = ? AND " + COURIER_EMAIL + " = ?",
                    new String[]{TaskState.IN_WAREHOUSE.toString(), TaskState.ON_THE_WAY.toString(), Integer.toString(warehouseId), courierEmail},
                    null, null, null);
        }

        if (cursor.moveToFirst())
        {
            int taskIDColIndex = cursor.getColumnIndex(ID);

            do
            {
                int id = cursor.getInt(taskIDColIndex);
                Task task = new Task(id);

                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();

        DatabaseManager.getInstance().closeDatabase();
        return tasks;
    }

    public static void add(NodeList tasks, String userEmail)
    {
        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();

        for (int i = 0; i < tasks.getLength(); i++)
        {
            Node task = tasks.item(i);

            if (task.getNodeType() == Node.ELEMENT_NODE)
            {
                Element element = (Element) task;

                Cursor cursor = database.query(TABLE_NAME, new String[]{ID}, ID + " = ?",
                        new String[]{element.getAttribute(ID)}, null, null, null);
                if(cursor.getCount() == 0)
                {
                    contentValues.put(ID, Integer.parseInt(element.getAttribute(ID)));
                    contentValues.put(STATE, TaskState.IN_WAREHOUSE.toString());
                    contentValues.put(CONTENT, element.getAttribute(CONTENT));
                    contentValues.put(RECEIVER_ID, Integer.parseInt(element.getAttribute(RECEIVER_ID)));
                    contentValues.put(SENDER_ID, Integer.parseInt(element.getAttribute(SENDER_ID)));
                    contentValues.put(COURIER_EMAIL, userEmail);
                    contentValues.put(ADDRESS, element.getAttribute(ADDRESS));
                    contentValues.put(LATITUDE, Float.parseFloat(element.getAttribute(LATITUDE)));
                    contentValues.put(LONGITUDE, Float.parseFloat(element.getAttribute(LONGITUDE)));
                    contentValues.put(WAREHOUSE_ID, Integer.parseInt(element.getAttribute(WAREHOUSE_ID)));
                    contentValues.put(DATE, element.getAttribute(DATE));
                    contentValues.put(COMMENT, element.getAttribute(COMMENT));
                    contentValues.put(CODE, element.getAttribute(CODE));

                    database.insert(Task.TABLE_NAME, null, contentValues);
                }
                cursor.close();
            }
        }
        DatabaseManager.getInstance().closeDatabase();
    }
}
