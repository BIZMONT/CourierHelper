package com.bizmont.courierhelper.OtherStuff;

import android.content.SharedPreferences;

public class Courier {
    private static Courier ourInstance = new Courier();

    private String name;
    private CourierState state;

    private Courier()
    {
        name = "Unknown";
        state = CourierState.NOT_ACTIVE;
    }

    private Courier(String name, CourierState state)
    {
        this.name = name;
        this.state = state;
    }

    public static void initializeInstance(String name, CourierState state)
    {
        ourInstance = new Courier(name,state);
    }

    public static Courier getInstance() {
        return ourInstance;
    }

    public String getName() {
        return name;
    }

    public CourierState getState() {
        return state;
    }

    public void setState(CourierState state)
    {
        this.state = state;
    }
}

