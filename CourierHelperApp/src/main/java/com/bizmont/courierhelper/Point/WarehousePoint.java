package com.bizmont.courierhelper.Point;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Task.Task;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;

public class WarehousePoint extends Point
{
    private List<Task> tasks;

    public WarehousePoint(String address, double latitude, double longitude, float radius, int ID)
    {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.ID = ID;
    }

    @Override
    public Marker InitializeMarker(Context context, MapView map)
    {
        marker = new Marker(map);
        marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_warehouse));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setPosition(new GeoPoint(latitude,longitude));
        marker.setTitle(address);
        return marker;
    }
}
