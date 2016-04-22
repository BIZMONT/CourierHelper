package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.OtherStuff.TaskPassDecoder;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTrackerService;
import com.bizmont.courierhelper.Task.TaskState;

public class CompleteTaskActivity extends AppCompatActivity
{
    EditText codeEdit;
    EditText reasonEdit;
    LinearLayout reasonLayout;
    LinearLayout codeLayout;
    ImageView correctCode;

    boolean isComplete;

    int id;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_task_activity);

        isComplete = false;

        Intent intent = getIntent();
        id = intent.getIntExtra("ID",0);
        setTitle("Complete task #" + id);

        codeEdit = (EditText)findViewById(R.id.complete_code);
        reasonEdit = (EditText)findViewById(R.id.complete_reason);
        reasonLayout = (LinearLayout)findViewById(R.id.complete_with_reason);
        codeLayout = (LinearLayout)findViewById(R.id.complete_with_code);
        correctCode = (ImageView)findViewById(R.id.complete_correct);

        code = DataBase.getTaskCode(id);
    }

    public void onCheckButtonClick(View view)
    {
        if(TaskPassDecoder.isMatches(String.valueOf(id), code, codeEdit.getText().toString()))
        {
            correctCode.setVisibility(View.VISIBLE);
            codeLayout.setEnabled(false);
            isComplete = true;
        }
        else
        {
            Toast.makeText(this, R.string.wrong_code,Toast.LENGTH_SHORT).show();
        }
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
        if(isComplete)
        {
            DataBase.setTaskStatus(TaskState.DELIVERED, id);
        }
        else
        {
            DataBase.setTaskStatus(TaskState.NOT_DELIVERED, id);
        }
        Intent intent = new Intent(GPSTrackerService.BROADCAST_RECEIVE_ACTION);
        intent.putExtra("Update points", true);
        onBackPressed();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
