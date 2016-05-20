package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.ExtrasNames;
import com.bizmont.courierhelper.Models.Task.TaskFullDetails;
import com.bizmont.courierhelper.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class TaskDetailsActivity extends AppCompatActivity
{
    TextView senderName;
    TextView senderAddress;
    TextView senderPhone;
    TextView receiverName;
    TextView address;
    TextView receiverPhone;
    TextView content;
    TextView date;
    TextView warehouseAddress;
    TextView state;
    TextView comment;

    MapView map;
    IMapController mapController;

    TaskFullDetails details;
    private  static final String LOG_TAG = "Task details activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details_activity);

        Intent intent = getIntent();

        details = null;
        int id = intent.getIntExtra(ExtrasNames.TASK_ID,0);
        Log.d(LOG_TAG, "Details for task#" + id);

        if(id != 0)
        {
            details = DataBase.getFullTaskDetails(id);

            if(details != null)
            {
                setTitle(getString(R.string.task_number) + details.getId());
                senderName = (TextView) findViewById(R.id.sender_name);
                senderName.setText(details.getSenderName());

                senderAddress = (TextView) findViewById(R.id.sender_address);
                senderAddress.setText(details.getSenderAddress());

                senderPhone = (TextView) findViewById(R.id.sender_phone);
                senderPhone.setText(details.getSenderPhone());

                receiverName = (TextView) findViewById(R.id.receiver_name);
                receiverName.setText(details.getReceiverName());
                address = (TextView)findViewById(R.id.map_address_textview);
                address.setText(details.getAddress());
                receiverPhone = (TextView) findViewById(R.id.receiver_phone);
                receiverPhone.setText(details.getReceiverPhone());

                content = (TextView) findViewById(R.id.task_content);
                content.setText(details.getContent());

                date = (TextView) findViewById(R.id.task_date);
                date.setText(details.getDate());
                warehouseAddress = (TextView) findViewById(R.id.task_warehouse);
                warehouseAddress.setText(details.getWarehouseAddress());
                state = (TextView) findViewById(R.id.task_state);
                state.setText(details.getState().toString());

                comment = (TextView) findViewById(R.id.task_comment);
                comment.setText(details.getComment());

                map = (MapView) findViewById(R.id.map);
                assert map != null;
                map.setTileSource(TileSourceFactory.MAPQUESTOSM);
                map.setMaxZoomLevel(18);
                map.setMinZoomLevel(18);
                map.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                map.getOverlays().add(details.createMarker(this,map));

                mapController = map.getController();
                mapController.setZoom(18);
                mapController.setCenter(new GeoPoint(details.getLatitude(),details.getLongitude()));
            }
        }
    }
}
