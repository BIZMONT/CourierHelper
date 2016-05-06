package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Task.TaskDetails;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.R;

public class TaskDetailsActivity extends AppCompatActivity
{
    TaskDetails details;
    private  static final String LOG_TAG = "Task details activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_info_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        details = null;
        int id = intent.getIntExtra(ExtrasNames.TASK_ID,0);
        Log.d(LOG_TAG, "Details for task#" + id);

        if(id != 0)
        {
            details = DataBase.getTaskDetails(id);

            TextView taskId = (TextView)findViewById(R.id.task_id);
            taskId.setText(String.valueOf(details.getId()));
            TextView senderName = (TextView)findViewById(R.id.sender_name);
            senderName.setText(details.getSenderName());
            TextView senderAddress = (TextView)findViewById(R.id.sender_address);
            senderAddress.setText(details.getSenderAddress());
            TextView senderPhone = (TextView)findViewById(R.id.sender_phone);
            senderPhone.setText(details.getSenderPhone());

            TextView receiverName = (TextView)findViewById(R.id.receiver_name);
            receiverName.setText(details.getReceiverName());
            TextView receiverAddress = (TextView)findViewById(R.id.receiver_address);
            receiverAddress.setText(details.getAddress());
            TextView receiverPhone = (TextView)findViewById(R.id.receiver_phone);
            receiverPhone.setText(details.getReceiverPhone());

            TextView content = (TextView)findViewById(R.id.task_content);
            content.setText(details.getContent());

            TextView date = (TextView)findViewById(R.id.task_date);
            date.setText(details.getDate());
            TextView warehouseAddress = (TextView)findViewById(R.id.task_warehouse);
            warehouseAddress.setText(details.getWarehouseAddress());
            TextView state = (TextView)findViewById(R.id.task_state);
            state.setText(details.getState().toString());

            TextView comment = (TextView)findViewById(R.id.task_comment);
            comment.setText(details.getComment());
        }
    }
}
