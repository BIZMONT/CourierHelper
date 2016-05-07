package com.bizmont.courierhelper.Models.Receiver;

public class Receiver
{
    private int id;
    private String name;
    private String phone;

    public Receiver(int id, String name, String phone)
    {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
}
