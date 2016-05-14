package com.bizmont.courierhelper.Models.Task;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Point;
import com.bizmont.courierhelper.Models.Warehouse.Warehouse;
import com.bizmont.courierhelper.R;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class Task extends Point
{
    protected TaskState state;
    protected Warehouse warehouse;

    public Task(int id, String address, double latitude, double longitude, TaskState state,
                int warehouseId)
    {
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.warehouse = DataBase.getWarehouse(warehouseId);
    }

    public TaskState getState()
    {
        return state;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    @Override
    public Marker createMarker(Context context, final MapView map)
    {
        Marker marker = new Marker(map);
        switch (state)
        {
            case DELIVERED:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_green));
                break;
            case NOT_DELIVERED:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_red));
                break;
            case IN_WAREHOUSE:
                marker.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_task_yellow));
                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        map.getController().animateTo(new GeoPoint(warehouse.getLatitude(), warehouse.getLongitude()));
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
        marker.setTitle(context.getString(R.string.task_number) + Integer.toString(id));
        marker.setSubDescription(context.getString(R.string.address) + ": " + address);
        return marker;
    }
}
