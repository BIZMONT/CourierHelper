package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTrackerService;
import com.bizmont.courierhelper.Task.Task;
import com.bizmont.courierhelper.Task.TaskState;

public class WarehouseTasksListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private Task[] tasks = null;

    public WarehouseTasksListViewAdapter(Context context, int layoutResourceId, Task[] tasks)
    {
        super(context, layoutResourceId, tasks);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        final Holder holder;

        if(row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.taskID = (TextView) row.findViewById(R.id.task_id);
            holder. checkBox = (CheckBox) row.findViewById(R.id.task_checkbox);

            row.setTag(holder);
        }
        else
        {
            holder = (Holder)row.getTag();
        }

        holder.taskID.setText(String.valueOf(tasks[position].getId()));
        holder.checkBox.setId(tasks[position].getId());
        if (tasks[position].getState() == TaskState.ON_THE_WAY)
        {
            holder.checkBox.setChecked(true);
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.isChecked())
                {
                    DataBase.setTaskStatus(TaskState.ON_THE_WAY, checkBox.getId());
                }
                else
                {
                    DataBase.setTaskStatus(TaskState.IN_WAREHOUSE, checkBox.getId());
                }

                Intent locationIntent = new Intent(GPSTrackerService.BROADCAST_RECEIVE_ACTION);
                locationIntent.putExtra("Update points", true);
                context.sendBroadcast(locationIntent);
            }
        });
        return row;
    }

    static class Holder
    {
        TextView taskID;
        CheckBox checkBox;
    }
}
