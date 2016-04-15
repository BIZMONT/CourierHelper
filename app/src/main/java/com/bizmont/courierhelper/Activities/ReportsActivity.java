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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmont.courierhelper.Adapters.ReportAdapter;
import com.bizmont.courierhelper.OtherStuff.Courier;
import com.bizmont.courierhelper.OtherStuff.TaskState;
import com.bizmont.courierhelper.OtherStuff.Report;
import com.bizmont.courierhelper.R;

import java.util.Calendar;

public class ReportsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    static final int DATE_DIALOG_ID = 0;

    Button datePickerButton;
    ListView reportsList;

    int year;
    int month;
    int day;

    NavigationView navigationView;
    ReportAdapter reportAdapter;

    Report[] reports = new Report[]
            {
                    new Report(123,"Навушники", TaskState.DELIVERED),
                    new Report(456,"Телефон", TaskState.DELIVERED),
                    new Report(789,"Гомно", TaskState.NOT_DELIVERED)
            };

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
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_reports).setChecked(true);
        View headerView = navigationView.getHeaderView(0);
        ((TextView)headerView.findViewById(R.id.courier_name)).setText(Courier.getInstance().getName());
        ((TextView)headerView.findViewById(R.id.courier_status)).setText(Courier.getInstance().getState().toString());


        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerButton = (Button)findViewById(R.id.date_picker_button);
        datePickerButton.setText(day + "/" + (month) + "/" + year);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        reportsList = (ListView) findViewById(R.id.reports_listview);
        reportAdapter = new ReportAdapter(ReportsActivity.this, R.layout.reports_listview_row, reports);
        reportsList.setAdapter(reportAdapter);

        reportsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ReportsActivity.this,"Hello",Toast.LENGTH_SHORT).show();
            }
        });
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_get_tasks)
        {
            Intent intent = new Intent(this, GetTasksActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_map)
        {
            Intent intent = new Intent(this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else if (id == R.id.nav_tasks)
        {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            ReportsActivity.this.year = year;
            ReportsActivity.this.month = monthOfYear + 1;
            ReportsActivity.this.day = dayOfMonth;
            datePickerButton.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

            reports = new Report[]{
                    new Report(987,"Курваааа", TaskState.DELIVERED)
            };
            reportAdapter = new ReportAdapter(ReportsActivity.this, R.layout.reports_listview_row, reports);
            reportsList.setAdapter(reportAdapter);
        }
    };
}
