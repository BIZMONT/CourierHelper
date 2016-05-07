package com.bizmont.courierhelper.Models.Sender;


public class Sender
{
    private int id;
    private String name;
    private String phone;
    private String address;

    public Sender(int id, String name, String address, String phone)
    {
        this.id = id;
        this.name = name;
        this.address = address;
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
    public String getAddress() {
        return address;
    }
}
