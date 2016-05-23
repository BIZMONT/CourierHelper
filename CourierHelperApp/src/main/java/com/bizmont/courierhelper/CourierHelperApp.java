package com.bizmont.courierhelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.bizmont.courierhelper.DataBase.DatabaseManager;
import com.bizmont.courierhelper.Model.Courier.Courier;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class CourierHelperApp extends Application
{
    String currentUserEmail;
    @Override
    public void onCreate()
    {
        super.onCreate();

        currentUserEmail = getUsername() + "@gmail.com";

        DatabaseManager.initializeDatabase(getApplicationContext());

        Courier.add(currentUserEmail,"Mr Smith");


        File path = new File(getFilesDir(), "kml/tracks");
        if(!path.exists())
        {
            path.mkdirs();
        }
        path = new File(getFilesDir(),"kml/recommended_paths");
        if(!path.exists())
        {
            path.mkdirs();
        }
    }

    public String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                return parts[0];
        }
        return getString(R.string.unknown);
    }
    public String getCurrentUserEmail()
    {
        return currentUserEmail;
    }
    public void setCurrentUserEmail(String currentUserEmail)
    {
        this.currentUserEmail = currentUserEmail;
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        DatabaseManager.releaseDatabase();
    }
}
