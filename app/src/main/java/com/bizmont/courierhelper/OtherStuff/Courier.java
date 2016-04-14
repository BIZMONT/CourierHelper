package com.bizmont.courierhelper.OtherStuff;

public class Courier {
    private static Courier ourInstance = new Courier();

    public static Courier getInstance() {
        return ourInstance;
    }

    private String name;
    private CourierState state;

    private Courier()
    {
        name = "BIZMONT";
        state = CourierState.NOT_ACTIVE;
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

