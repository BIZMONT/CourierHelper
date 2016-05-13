package com.bizmont.courierhelper.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.bizmont.courierhelper.Adapters.ReportsListViewAdapter;
import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.DatePickerFragment;
import com.bizmont.courierhelper.Models.Courier.Courier;
import com.bizmont.courierhelper.Models.Report.Report;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ReportsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button datePickerButton;
    ListView reportsList;
    TextView message;

    NavigationView navigationView;
    ReportsListViewAdapter reportsListViewAdapter;

    ArrayList<Report> reports;
    String pickedDate;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        ((TextView) headerView.findViewById(R.id.courier_name)).setText(Courier.getInstance().getName());
        ((TextView) headerView.findViewById(R.id.courier_status)).setText(Courier.getInstance().getState().toString());

        message = (TextView) findViewById(R.id.empty_list);

        Calendar c = Calendar.getInstance();
        pickedDate = simpleDateFormat.format(c.getTime());

        datePickerButton = (Button) findViewById(R.id.date_picker_button);
        assert datePickerButton != null;
        datePickerButton.setText(pickedDate);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
                newFragment.setCallBack(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        pickedDate = simpleDateFormat.format(c.getTime());
                        datePickerButton.setText(pickedDate);
                        createReportsList();
                    }
                });
            }
        });
        reportsList = (ListView) findViewById(R.id.reports_listview);
        reportsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), ReportDetailsActivity.class);
                    intent.putExtra(ExtrasNames.REPORT_ID, reports.get(position).getID());
                    startActivity(intent);
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        createReportsList();
        if (!navigationView.getMenu().findItem(R.id.nav_reports).isChecked()) {
            navigationView.getMenu().findItem(R.id.nav_reports).setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_menu_get_tasks_from_file || super.onOptionsItemSelected(item);

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
        else if(id == R.id.nav_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.nav_about)
        {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createReportsList()
    {
        reports = DataBase.getReportsWithDate(pickedDate);
        if(reports.size() != 0)
        {
            message.setVisibility(View.VISIBLE);
        }
        else
        {
            message.setVisibility(View.GONE);
        }
        reportsListViewAdapter = new ReportsListViewAdapter(ReportsActivity.this, R.layout.reports_listview_row, reports);
        reportsList.setAdapter(reportsListViewAdapter);
    }
}


