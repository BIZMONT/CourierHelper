package com.bizmont.courierhelper.OtherStuff;

public class Task
{
    protected int id;
    protected String deliveryName;
    protected TaskState state;
    private String recipientName;
    private String address;
    private float addressLatitude;
    private float addressLongitude;
    private String warehouseAddress;
    private float warehouseLatitude;
    private float warehouseLongitude;

    private String date;

    public int getId()
    {
        return id;
    }
    public TaskState getState()
    {
        return state;
    }
    public void setState(TaskState state) {
        this.state = state;
    }
    public String getDeliveryName()
    {
        return deliveryName;
    }
    public String getRecipientName() {
        return recipientName;
    }
    public String getAddress() {
        return address;
    }
    public float getAddressLatitude() {
        return addressLatitude;
    }
    public float getAddressLongitude() {
        return addressLongitude;
    }
    public String getWarehouseAddress() {
        return warehouseAddress;
    }
    public float getWarehouseLatitude() {
        return warehouseLatitude;
    }
    public float getWarehouseLongitude() {
        return warehouseLongitude;
    }
    public String getDate() {
        return date;
    }

}

