package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bizmont.courierhelper.OtherStuff.TaskState;
import com.bizmont.courierhelper.OtherStuff.Task;
import com.bizmont.courierhelper.R;

public class TasksAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private Task[] tasks = null;

    public TasksAdapter(Context context, int layoutResourceId, Task[] tasks)
    {
        super(context, layoutResourceId, tasks);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TaskHolder holder;

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

        Task task = tasks[position];
        holder.taskID.setText(String.valueOf(task.getId()));
        holder.taskState.setText(task.getState().toString());
        if(task.getState() == TaskState.DELIVERED)
        {
            holder.taskState.setTextColor(Color.GREEN);
        }
        else
        {
            holder.taskState.setTextColor(Color.RED);
        }
        holder.taskName.setText(task.getAddress());

        return row;
    }

    static class TaskHolder
    {
        TextView taskID;
        TextView taskName;
        TextView taskState;
    }
}

