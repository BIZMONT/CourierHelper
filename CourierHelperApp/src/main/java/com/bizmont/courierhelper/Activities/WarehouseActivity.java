package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bizmont.courierhelper.Adapters.WarehouseTasksListViewAdapter;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.R;
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
        int id = intent.getIntExtra("ID",0);
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
                intent.putExtra("id", tasks[position].getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause()
    {

        super.onPause();
    }
}
