package com.bizmont.courierhelper.DataBase;

import android.util.Log;

import com.bizmont.courierhelper.Model.Receiver;
import com.bizmont.courierhelper.Model.Sender;
import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.Model.Warehouse;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public final class Database
{
    static final String LOG_TAG = "Data Base";

    public static void addData(File file, String userEmail)
    {
        //TODO: catch parsing exceptions
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();

            NodeList table = document.getElementsByTagName("Task");

            Task.add(table, userEmail);
            Log.d(LOG_TAG,"Tasks added!");

            table = document.getElementsByTagName("Warehouse");
            Warehouse.add(table);
            Log.d(LOG_TAG,"Warehouses added!");

            table = document.getElementsByTagName("Receiver");
            Receiver.add(table);
            Log.d(LOG_TAG,"Receivers added!");

            table = document.getElementsByTagName("Sender");
            Sender.add(table);
            Log.d(LOG_TAG,"Senders added!");
        }
        catch (Exception e)
        {
            Log.d(LOG_TAG,"Error in parsing file: " + e.getMessage());
        }
    }
}

