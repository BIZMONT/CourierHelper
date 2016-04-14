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
            holder.orderID = (TextView)row.findViewById(R.id.order_id);
            holder.orderState = (TextView)row.findViewById(R.id.order_state);
            holder.orderName = (TextView)row.findViewById(R.id.order_name);

            row.setTag(holder);
        }
        else
        {
            holder = (TaskHolder)row.getTag();
        }

        Task task = tasks[position];
        /*holder.orderID.setText(String.valueOf(report.getId()));
        holder.orderState.setText(report.getState().toString());
        if(report.getState() == TaskState.DELIVERED)
        {
            holder.orderState.setTextColor(Color.GREEN);
        }
        else
        {
            holder.orderState.setTextColor(Color.RED);
        }
        holder.orderName.setText(report.getDeliveryName());
*/
        return row;
    }

    static class TaskHolder
    {
        TextView orderID;
        TextView orderName;
        TextView orderState;
    }
}

