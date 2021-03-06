package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.Model.Task.TaskState;
import com.bizmont.courierhelper.R;

import java.util.ArrayList;

public class WarehouseTasksListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Task> tasks = null;

    public WarehouseTasksListViewAdapter(Context context, int layoutResourceId, ArrayList<Task>  tasks)
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

        holder.taskID.setText(String.valueOf(tasks.get(position).getId()));
        if (tasks.get(position).getState() == TaskState.ON_THE_WAY)
        {
            holder.checkBox.setChecked(true);
        }

        holder.checkBox.setOnClickListener(new CheckBoxOnClickListener(tasks.get(position).getId()) {
            @Override
            public void onClick(View v)
            {
                CheckBox checkBox = (CheckBox) v;
                Task task = new Task(taskID);
                if(checkBox.isChecked())
                {
                    task.setState(TaskState.ON_THE_WAY);
                    //DataBase.setTaskState(TaskState.ON_THE_WAY, taskID);
                }
                else
                {
                    task.setState(TaskState.IN_WAREHOUSE);
                    //DataBase.setTaskState(TaskState.IN_WAREHOUSE, taskID);
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
