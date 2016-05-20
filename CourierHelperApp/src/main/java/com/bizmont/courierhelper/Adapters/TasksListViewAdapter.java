package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bizmont.courierhelper.Activities.CompleteTaskActivity;
import com.bizmont.courierhelper.Activities.MapActivity;
import com.bizmont.courierhelper.ExtrasNames;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.Models.Task.TaskState;
import com.bizmont.courierhelper.R;

import java.util.ArrayList;

public class TasksListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Task> tasks = null;

    public TasksListViewAdapter(Context context, int layoutResourceId, ArrayList<Task> tasks)
    {
        super(context, layoutResourceId, tasks);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final TaskHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TaskHolder();
            holder.taskID = (TextView)row.findViewById(R.id.task_id);
            holder.taskState = (TextView)row.findViewById(R.id.task_state);
            holder.taskName = (TextView)row.findViewById(R.id.task_name);

            row.setTag(holder);
        }
        else
        {
            holder = (TaskHolder)row.getTag();
        }

        final Task task = tasks.get(position);
        holder.taskID.setText(String.valueOf(task.getId()));
        holder.taskState.setText(task.getState().toString());
        holder.taskName.setText(task.getAddress());
        holder.taskName.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        ImageButton showOnMap = (ImageButton)row.findViewById(R.id.task_row_show);
        showOnMap.setOnClickListener(new ImageButtonOnClickListener(task.getId()) {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra(ExtrasNames.TASK_ID,taskID);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });

        ImageButton complete = (ImageButton)row.findViewById(R.id.task_row_complete);
        complete.setOnClickListener(new ImageButtonOnClickListener(task.getId()) {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, CompleteTaskActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(ExtrasNames.TASK_ID, taskID);
                context.startActivity(intent);
            }
        });
        if(task.getState() == TaskState.ON_THE_WAY)
        {
            complete.setVisibility(View.VISIBLE);
        }
        return row;
    }

    static class TaskHolder
    {
        TextView taskID;
        TextView taskName;
        TextView taskState;
    }

    private class ImageButtonOnClickListener implements View.OnClickListener
    {
        int taskID;
        public ImageButtonOnClickListener(int taskID)
        {
            this.taskID = taskID;
        }

        @Override
        public void onClick(View v)
        {
        }
    }
}

