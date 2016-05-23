package com.bizmont.courierhelper.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmont.courierhelper.CourierHelperApp;
import com.bizmont.courierhelper.Fragments.DatePickerFragment;
import com.bizmont.courierhelper.Model.Courier.Courier;
import com.bizmont.courierhelper.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StatisticsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Button fromDatePicker;
    private Button toDatePicker;
    private TextView emptyMessage;

    private Date toDatePicked;
    private Date fromDatePicked;

    NavigationView navigationView;

    Calendar calendar;

    GraphView graph;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        calendar = Calendar.getInstance();
        graph = (GraphView)findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(5);
        series.setColor(ContextCompat.getColor(this,R.color.primary));
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(StatisticsActivity.this, "Tasks: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
        graph.addSeries(series);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        emptyMessage = (TextView) findViewById(R.id.empty_list);

        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        fromDatePicked = calendar.getTime();
        calendar.setTime(fromDatePicked);
        calendar.add(Calendar.DATE,1);
        toDatePicked = calendar.getTime();

        fromDatePicker = (Button) findViewById(R.id.from_date_picker);
        String fromDateText = simpleDateFormat.format(fromDatePicked);
        fromDatePicker.setText(fromDateText);

        toDatePicker = (Button) findViewById(R.id.to_date_picker);
        String toDateText = simpleDateFormat.format(toDatePicked);
        toDatePicker.setText(toDateText);

        fromDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
                newFragment.setCallBack(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth,0,0,0);
                        long current = calendar.getTime().getTime();
                        if(current < toDatePicked.getTime())
                        {
                            fromDatePicked = calendar.getTime();
                            fromDatePicker.setText(simpleDateFormat.format(fromDatePicked));
                            createStatistic();
                        }
                    }
                });
            }
        });
        toDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
                newFragment.setCallBack(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth,0,0,0);
                        long current = calendar.getTime().getTime();
                        if(fromDatePicked.getTime() < current)
                        {
                            toDatePicked = calendar.getTime();
                            toDatePicker.setText(simpleDateFormat.format(toDatePicked));
                            createStatistic();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!navigationView.getMenu().findItem(R.id.nav_stats).isChecked()) {
            navigationView.getMenu().findItem(R.id.nav_stats).setChecked(true);
        }
        View headerView = navigationView.getHeaderView(0);
        String userEmail = ((CourierHelperApp)getApplication()).getCurrentUserEmail();
        Courier courier = new Courier(userEmail);
        TextView name = (TextView) headerView.findViewById(R.id.courier_name);
        TextView email = (TextView) headerView.findViewById(R.id.courier_email);
        name.setText(courier.getName());
        email.setText(courier.getEmail());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }

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
        if (id == R.id.nav_reports)
        {
            Intent intent = new Intent(this, ReportsActivity.class);
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
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void createStatistic()
    {

    }
}
