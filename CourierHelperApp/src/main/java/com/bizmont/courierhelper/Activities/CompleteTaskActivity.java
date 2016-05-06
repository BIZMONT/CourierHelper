package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.OtherStuff.TaskCodeDecoder;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTracker;

public class CompleteTaskActivity extends AppCompatActivity
{
    EditText codeEdit;
    EditText reasonEdit;
    LinearLayout reasonLayout;
    LinearLayout codeLayout;
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
        codeLayout = (LinearLayout)findViewById(R.id.complete_with_code);
        correctCode = (ImageView)findViewById(R.id.complete_correct);

        code = DataBase.getTaskCode(taskId);
    }

    public void onTypeSwitched(View view)
    {
        Switch switchV =  (Switch) view;
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
            if(!TaskCodeDecoder.isMatches(String.valueOf(taskId), code, codeEdit.getText().toString()))
            {
                Toast.makeText(this, R.string.wrong_code,Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Please, enter reason", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        intent.putExtra(ExtrasNames.COMPLETE_TASK, taskId);
        onBackPressed();
    }
}
