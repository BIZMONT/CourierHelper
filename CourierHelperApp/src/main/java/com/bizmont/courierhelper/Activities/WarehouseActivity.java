package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bizmont.courierhelper.Adapters.WarehouseTasksListViewAdapter;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTracker;
import com.bizmont.courierhelper.Task.Task;

public class WarehouseActivity extends AppCompatActivity
{
    ListView tasksList;
    Task[] tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warehouse_activity);
        Intent intent = getIntent();
        int id = intent.getIntExtra(ExtrasNames.WAREHOUSE_ID, 0);
        setTitle("Warehouse #" + id);

        tasks = DataBase.getActiveTasks(id);

        tasksList = (ListView) findViewById(R.id.warehouse_listview);
        WarehouseTasksListViewAdapter warehouseTasksListViewAdapter = new WarehouseTasksListViewAdapter(this, R.layout.warehouse_content_row, tasks);
        tasksList.setAdapter(warehouseTasksListViewAdapter);
        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(getApplicationContext(), TaskDetailsActivity.class);
                intent.putExtra(ExtrasNames.TASK_ID, tasks[position].getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        Intent intent = new Intent(GPSTracker.BROADCAST_RECEIVE_ACTION);
        intent.putExtra(ExtrasNames.IS_CREATE_ROUTE, true);
        intent.putExtra(ExtrasNames.IS_UPDATE_POINTS, true);
        sendBroadcast(intent);
    }
}
