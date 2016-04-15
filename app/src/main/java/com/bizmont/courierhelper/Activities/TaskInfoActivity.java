package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.TaskDetails;
import com.bizmont.courierhelper.R;

public class TaskInfoActivity extends AppCompatActivity {

    TextView senderName;
    TextView senderPhone;
    TextView senderAddress;

    TextView receiverName;
    TextView receiverAddress;
    TextView receiverPhone;

    TextView content;
    TextView date;
    TextView state;
    TextView warehouseAddress;

    TextView comment;

    TaskDetails details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_info_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        details = null;
        int id = intent.getIntExtra("id",0);

        if(id != 0)
        {
            details = DataBase.getTaskDetails(id);
        }

        senderName = (TextView)findViewById(R.id.sender_name);
        senderName.setText(details.getSenderName());
        senderAddress = (TextView)findViewById(R.id.sender_address);
        senderAddress.setText(details.getSenderAddress());
        senderPhone = (TextView)findViewById(R.id.sender_phone);
        senderPhone.setText(details.getSenderPhone());

        receiverName = (TextView)findViewById(R.id.receiver_name);
        receiverName.setText(details.getReceiverName());
        receiverAddress = (TextView)findViewById(R.id.receiver_address);
        receiverAddress.setText(details.getAddress());
        receiverPhone = (TextView)findViewById(R.id.receiver_phone);
        receiverPhone.setText(details.getReceiverPhone());

        content = (TextView)findViewById(R.id.task_content);
        content.setText(details.getContent());

        date = (TextView)findViewById(R.id.task_date);
        date.setText(details.getDate());
        warehouseAddress = (TextView)findViewById(R.id.task_warehouse);
        warehouseAddress.setText(details.getWarehouseAddress());
        state = (TextView)findViewById(R.id.task_state);
        state.setText(details.getState().toString());

        comment = (TextView)findViewById(R.id.task_comment);
        comment.setText(details.getComment());
    }
}
