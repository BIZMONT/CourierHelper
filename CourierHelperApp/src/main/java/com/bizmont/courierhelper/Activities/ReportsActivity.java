package com.bizmont.courierhelper.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.bizmont.courierhelper.Adapters.ReportsListViewAdapter;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Courier.Courier;
import com.bizmont.courierhelper.Models.Report.Report;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.R;

import java.util.ArrayList;
import java.util.Calendar;

public class ReportsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    static final int DATE_DIALOG_ID = 0;

    Button datePickerButton;
    ListView reportsList;
    TextView message;

    int year;
    int month;
    int day;

    NavigationView navigationView;
    ReportsListViewAdapter reportsListViewAdapter;

    ArrayList<Report> reports;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_reports).setChecked(true);
        View headerView = navigationView.getHeaderView(0);
        ((TextView)headerView.findViewById(R.id.courier_name)).setText(Courier.getInstance().getName());
        ((TextView)headerView.findViewById(R.id.courier_status)).setText(Courier.getInstance().getState().toString());

        message = (TextView)findViewById(R.id.empty_list);

        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        String m = String.valueOf(month);
        String d = String.valueOf(day);
        if(month < 10){

            m = "0" + (month + 1);
        }
        if(day < 10){

            d  = "0" + day ;
        }

        datePickerButton = (Button)findViewById(R.id.date_picker_button);
        assert datePickerButton != null;
        datePickerButton.setText(year + "-" + m + "-" + d);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        Log.d("Date", datePickerButton.getText().toString());

        reports = DataBase.getReportsWithDate(datePickerButton.getText().toString());

        if(reports != null) {
            message.setVisibility(View.GONE);
            reportsList = (ListView) findViewById(R.id.reports_listview);
            reportsListViewAdapter = new ReportsListViewAdapter(ReportsActivity.this, R.layout.reports_listview_row, reports);
            reportsList.setAdapter(reportsListViewAdapter);

            reportsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), ReportDetailsActivity.class);
                    intent.putExtra(ExtrasNames.REPORT_ID, reports.get(position).getID());
                    startActivity(intent);
                }
            });
        }
        else
        {
            message.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!navigationView.getMenu().findItem(R.id.nav_reports).isChecked()) {
            navigationView.getMenu().findItem(R.id.nav_reports).setChecked(true);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_menu_get_tasks_from_file) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_map)
        {
            Intent intent = new Intent(this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else if (id == R.id.nav_tasks)
        {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        if(id == DATE_DIALOG_ID)
        {
            return new DatePickerDialog(this, dPickListener, this.year, this.month, this.day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener dPickListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            String m = String.valueOf(monthOfYear);
            String d = String.valueOf(dayOfMonth);

            ReportsActivity.this.year = year;
            ReportsActivity.this.month = monthOfYear + 1;
            ReportsActivity.this.day = dayOfMonth;

            if(monthOfYear < 10){

                m = "0" + (monthOfYear + 1);
            }
            if(dayOfMonth < 10){

                d  = "0" + dayOfMonth ;
            }
            datePickerButton.setText(year + "-" + m + "-" + d);
            reports = DataBase.getReportsWithDate(datePickerButton.getText().toString());
            reportsListViewAdapter = new ReportsListViewAdapter(ReportsActivity.this, R.layout.reports_listview_row, reports);
            reportsList.setAdapter(reportsListViewAdapter);
        }
    };
}
