package com.bizmont.courierhelper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bizmont.courierhelper.Models.Report.Report;
import com.bizmont.courierhelper.Models.Task.TaskState;
import com.bizmont.courierhelper.R;

import java.util.ArrayList;

public class ReportsListViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<Report> reports = null;

    public ReportsListViewAdapter(Context context, int layoutResourceId, ArrayList<Report> reports)
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
            holder.orderID = (TextView)row.findViewById(R.id.task_id);
            holder.orderState = (TextView)row.findViewById(R.id.task_state);
            holder.orderName = (TextView)row.findViewById(R.id.task_name);

            row.setTag(holder);
        }
        else
        {
            holder = (ReportHolder)row.getTag();
        }

        Report report = reports.get(position);
        holder.orderID.setText(String.valueOf(report.getTaskId()));
        if(report.getReason() != null)
        {
            holder.orderState.setText(TaskState.NOT_DELIVERED.toString());
        }
        else
        {
            holder.orderState.setText(TaskState.DELIVERED.toString());
        }

        return row;
    }

    static class ReportHolder
    {
        TextView orderID;
        TextView orderName;
        TextView orderState;
    }
}
