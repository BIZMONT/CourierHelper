package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bizmont.courierhelper.CourierHelperApp;
import com.bizmont.courierhelper.ExtrasNames;
import com.bizmont.courierhelper.Model.Courier.Courier;
import com.bizmont.courierhelper.Model.Courier.CourierState;
import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTracker;
import com.bizmont.courierhelper.TaskCodeDecoder;

public class CompleteTaskActivity extends AppCompatActivity
{
    EditText codeEdit;
    EditText reasonEdit;
    LinearLayout reasonLayout;
    RelativeLayout codeLayout;
    ImageView correctCode;

    boolean isComplete;

    int taskId;
    String code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_task_activity);

        isComplete = false;

        Intent intent = getIntent();
        taskId = intent.getIntExtra(ExtrasNames.TASK_ID,0);
        setTitle("Complete task #" + taskId);

        codeEdit = (EditText)findViewById(R.id.complete_code);
        reasonEdit = (EditText)findViewById(R.id.complete_reason);
        reasonLayout = (LinearLayout)findViewById(R.id.complete_with_reason);
        codeLayout = (RelativeLayout)findViewById(R.id.complete_with_code);
        correctCode = (ImageView)findViewById(R.id.complete_correct);

        Task task = new  Task(taskId);
        code = task.getCode();

        codeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                EditText editText = (EditText)v;
                if(!hasFocus)
                {
                    if (editText.getText().length() == 8 &&
                            !TaskCodeDecoder.isMatches(String.valueOf(taskId), code, editText.getText().toString())) {
                        correctCode.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        correctCode.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    public void onTypeSwitched(View view)
    {
        Switch switchV = (Switch) view;
        if(switchV.isChecked())
        {
            reasonLayout.setVisibility(View.VISIBLE);
            codeLayout.setVisibility(View.GONE);
        }
        else
        {
            reasonLayout.setVisibility(View.GONE);
            codeLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onClickComplete(View view)
    {
        Intent intent = new Intent(GPSTracker.BROADCAST_RECEIVE_ACTION);
        if(codeLayout.getVisibility() == View.VISIBLE)
        {
            String userEmail = ((CourierHelperApp)getApplication()).getCurrentUserEmail();
            Courier courier = new Courier(userEmail);
            if(courier.getState() != CourierState.AT_THE_POINT)
            {
                Toast.makeText(this, R.string.not_at_point_alert, Toast.LENGTH_SHORT).show();
                return;
            }
            if(!TaskCodeDecoder.isMatches(String.valueOf(taskId), code, codeEdit.getText().toString()))
            {
                Toast.makeText(this, R.string.wrong_code, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else
        {
            if(reasonEdit.getText().length()>1)
            {
                intent.putExtra(ExtrasNames.REASON, reasonEdit.getText().toString());
            }
            else
            {
                Toast.makeText(this, R.string.reason_empty_alert_message, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        intent.putExtra(ExtrasNames.COMPLETE_TASK, taskId);
        sendBroadcast(intent);
        onBackPressed();
    }

    public void onClickTaskDetails(View view)
    {
        Intent intent = new Intent(getApplicationContext(), TaskDetailsActivity.class);
        intent.putExtra(ExtrasNames.TASK_ID, taskId);
        startActivity(intent);
    }
}
