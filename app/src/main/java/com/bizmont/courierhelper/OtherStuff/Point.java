package com.bizmont.courierhelper.OtherStuff;

public class Point
{
    private double latitude;
    private double longitude;

    public Point(double latitude, double longitude)
    {

    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
}

class WarehousePoint extends Point
{

    public WarehousePoint(double latitude, double longitude) {
        super(latitude, longitude);
    }
}
