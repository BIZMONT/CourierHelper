package com.bizmont.courierhelper.Models.Courier;

public class Courier
{
    String email;
    String name;
    CourierState state;

    public Courier(String email, String name, CourierState state)
    {
        this.email = email;
        this.name = name;
        this.state = state;
    }

    public CourierState getState() {
        return state;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
