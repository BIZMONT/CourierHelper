package com.bizmont.courierhelper.Models.Courier;

import com.bizmont.courierhelper.Models.CourierState;

import java.util.ArrayList;
import java.util.List;

public class Courier
{
    public interface CourierListener
    {
        void onStatusChanged();
    }

    private static List<CourierListener> listeners = new ArrayList<>();

    private static Courier ourInstance = new Courier();

    private String name;
    private CourierState state;

    private Courier()
    {
        name = "Unknown user";
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
        for(CourierListener cl: listeners)
        {
            cl.onStatusChanged();
        }
    }

    public static void addOnStatusChangedListener(CourierListener listener)
    {
        listeners.add(listener);
    }
}

