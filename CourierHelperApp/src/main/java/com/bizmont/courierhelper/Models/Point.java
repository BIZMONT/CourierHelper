package com.bizmont.courierhelper.Models;

import android.content.Context;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public abstract class Point
{
    protected int id;
    protected double latitude;
    protected double longitude;
    protected String address;
    protected float radius = 20;

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getAddress() {
        return address;
    }
    public float getRadius() {
        return radius;
    }
    public int getId() {
        return id;
    }

    public abstract Marker createMarker(Context context, MapView map);
}

