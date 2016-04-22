package com.bizmont.courierhelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.OtherStuff.CourierState;

import java.util.LinkedList;
import java.util.List;

public class CourierHelperApp extends Application
{
    @Override
    public void onCreate()
    {
        Courier.initializeInstance(getUsername(), CourierState.NOT_ACTIVE);
        super.onCreate();

        DataBase.initialize(getApplicationContext());

    }

    private String getUsername() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                return parts[0];
        }
        return getString(R.string.unknown_user);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        DataBase.close();
    }
}
