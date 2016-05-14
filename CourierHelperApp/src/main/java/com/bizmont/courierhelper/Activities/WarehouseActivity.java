package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bizmont.courierhelper.Adapters.WarehouseTasksListViewAdapter;
import com.bizmont.courierhelper.CourierHelperApp;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTracker;

import java.util.ArrayList;

public class WarehouseActivity extends AppCompatActivity
{
    ListView tasksList;
    ArrayList<Task> tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warehouse_activity);


        Intent intent = getIntent();
        int id = intent.getIntExtra(ExtrasNames.WAREHOUSE_ID, 0);
        setTitle("Warehouse #" + id);

        tasks = DataBase.getActiveTasks(id,((CourierHelperApp)getApplication()).getCurrentUserEmail());

        tasksList = (ListView) findViewById(R.id.warehouse_listview);
        WarehouseTasksListViewAdapter warehouseTasksListViewAdapter = new WarehouseTasksListViewAdapter(this, R.layout.warehouse_content_row, tasks);
        tasksList.setAdapter(warehouseTasksListViewAdapter);
        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(getApplicationContext(), TaskDetailsActivity.class);
                intent.putExtra(ExtrasNames.TASK_ID, tasks.get(position).getId());
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(GPSTracker.BROADCAST_RECEIVE_ACTION);
        intent.putExtra(ExtrasNames.IS_CREATE_ROUTE, true);
        intent.putExtra(ExtrasNames.IS_UPDATE_POINTS, true);
        sendBroadcast(intent);
    }
}
