package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.Models.Task.TaskState;
import com.bizmont.courierhelper.R;

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
        if (tasks[position].getState() == TaskState.ON_THE_WAY)
        {
            holder.checkBox.setChecked(true);
        }

        holder.checkBox.setOnClickListener(new CheckBoxOnClickListener(tasks[position].getId()) {
            @Override
            public void onClick(View v)
            {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.isChecked())
                {
                    DataBase.setTaskState(TaskState.ON_THE_WAY, taskID);
                }
                else
                {
                    DataBase.setTaskState(TaskState.IN_WAREHOUSE, taskID);
                }
            }
        });
        return row;
    }

    static class Holder
    {
        TextView taskID;
        CheckBox checkBox;
    }

    private class CheckBoxOnClickListener implements View.OnClickListener
    {
        int taskID;
        public CheckBoxOnClickListener(int taskID)
        {
            this.taskID = taskID;
        }

        @Override
        public void onClick(View v)
        {
        }
    }
}
