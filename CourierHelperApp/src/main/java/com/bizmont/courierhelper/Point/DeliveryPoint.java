package com.bizmont.courierhelper.Point;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bizmont.courierhelper.Models.Task.TaskState;
import com.bizmont.courierhelper.R;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class DeliveryPoint extends Point
{
    private TaskState state;
    private double warehouseLatitude;
    private double warehouseLongitude;

    public DeliveryPoint(String address, double latitude, double longitude, TaskState state, int ID,
                         double warehouseLatitude, double warehouseLongitude)
    {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.ID = ID;
        this.warehouseLatitude = warehouseLatitude;
        this.warehouseLongitude = warehouseLongitude;
    }

    public TaskState getState()
    {
        return state;
    }

    public int getID()
    {
        return ID;
    }

    @Override
    public Marker createMarker(Context context, final MapView map)
    {
        Marker marker = new Marker(map);
        switch (state)
        {
            case DELIVERED:
                //marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_map_task));
                break;
            case NOT_DELIVERED:
                //marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_map_task));
                break;
            case IN_WAREHOUSE:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_yellow));
                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        map.getController().animateTo(new GeoPoint(warehouseLatitude, warehouseLongitude));
                        return true;
                    }
                });
                break;
            case ON_THE_WAY:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_blue));
                break;
        }

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setPosition(new GeoPoint(latitude,longitude));
        marker.setTitle(context.getString(R.string.task_number) + Integer.toString(ID));
        marker.setSubDescription(context.getString(R.string.address) + ": " + address);
        return marker;
    }
}
