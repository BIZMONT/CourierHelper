package com.bizmont.courierhelper.Models.Warehouse;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bizmont.courierhelper.Models.Point;
import com.bizmont.courierhelper.R;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class Warehouse extends Point
{
    public Warehouse(int id, String address, double latitude, double longitude, float radius)
    {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.id = id;
    }

    @Override
    public Marker createMarker(Context context, MapView map)
    {
        Marker marker = new Marker(map);
        marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_warehouse));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setPosition(new GeoPoint(latitude,longitude));
        marker.setTitle(address);
        return marker;
    }
}
