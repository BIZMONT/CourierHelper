package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bizmont.courierhelper.Task.TaskReport;
import com.bizmont.courierhelper.Task.TaskState;
import com.bizmont.courierhelper.R;

public class ReportsListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private TaskReport[] reports = null;

    public ReportsListViewAdapter(Context context, int layoutResourceId, TaskReport[] reports)
    {
        super(context,layoutResourceId,reports);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.reports = reports;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ReportHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ReportHolder();
            holder.orderID = (TextView)row.findViewById(R.id.order_id);
            holder.orderState = (TextView)row.findViewById(R.id.order_state);
            holder.orderName = (TextView)row.findViewById(R.id.order_name);

            row.setTag(holder);
        }
        else
        {
            holder = (ReportHolder)row.getTag();
        }

        TaskReport report = reports[position];
        holder.orderID.setText(String.valueOf(report.getId()));
        holder.orderState.setText(report.getState().toString());
        if(report.getState() == TaskState.DELIVERED)
        {
            holder.orderState.setTextColor(Color.GREEN);
        }
        else
        {
            holder.orderState.setTextColor(Color.RED);
        }
        holder.orderName.setText(report.getAddress());

        return row;
    }

    static class ReportHolder
    {
        TextView orderID;
        TextView orderName;
        TextView orderState;
    }
}