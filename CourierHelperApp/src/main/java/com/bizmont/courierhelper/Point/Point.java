package com.bizmont.courierhelper.Point;

import android.content.Context;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public abstract class Point
{
    protected int ID;
    protected double latitude;
    protected double longitude;
    protected String address;
    protected Marker marker;
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
    public Marker getMarker() {
        return marker;
    }
    public float getRadius() {
        return radius;
    }

    public int getID() {
        return ID;
    }

    public abstract Marker InitializeMarker(Context context, MapView map);
}

