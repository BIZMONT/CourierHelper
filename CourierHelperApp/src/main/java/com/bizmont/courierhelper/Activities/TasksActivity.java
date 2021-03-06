package com.bizmont.courierhelper.Activities;

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
import android.widget.ListView;
import android.widget.TextView;

import com.bizmont.courierhelper.Adapters.TasksListViewAdapter;
import com.bizmont.courierhelper.CourierHelperApp;
import com.bizmont.courierhelper.DataBase.Database;
import com.bizmont.courierhelper.ExtrasNames;
import com.bizmont.courierhelper.FileChooser;
import com.bizmont.courierhelper.Model.Courier.Courier;
import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.R;
import com.bizmont.courierhelper.Services.GPSTracker;

import java.io.File;
import java.util.ArrayList;

public class TasksActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView emptyMessage;
    ListView tasksList;
    TasksListViewAdapter tasksListViewAdapter;
    NavigationView navigationView;

    ArrayList<Task> tasks;

    FileChooser fileChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileChooser = new FileChooser(this);
        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                Database.addData(file,((CourierHelperApp)getApplication()).getCurrentUserEmail());

                createTasksList();

                Intent locationIntent = new Intent(GPSTracker.BROADCAST_RECEIVE_ACTION);
                locationIntent.putExtra(ExtrasNames.IS_UPDATE_POINTS, true);
                sendBroadcast(locationIntent);
            }
        });
        fileChooser.setExtension("cht");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_tasks).setChecked(true);


        emptyMessage = (TextView)findViewById(R.id.empty_list);

        tasksList = (ListView) findViewById(R.id.tasks_listview);
        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), TaskDetailsActivity.class);
                intent.putExtra(ExtrasNames.TASK_ID, tasks.get(position).getId());
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        createTasksList();
        if (!navigationView.getMenu().findItem(R.id.nav_tasks).isChecked())
        {
            navigationView.getMenu().findItem(R.id.nav_tasks).setChecked(true);
        }
        View headerView = navigationView.getHeaderView(0);
        Courier courier = new Courier(((CourierHelperApp)getApplication()).getCurrentUserEmail());
        TextView name = (TextView) headerView.findViewById(R.id.courier_name);
        TextView email = (TextView) headerView.findViewById(R.id.courier_email);
        name.setText(courier.getName());
        email.setText(courier.getEmail());
    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.tasks, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_menu_get_tasks_from_file)
        {
            fileChooser.showDialog();
        }

        return super.onOptionsItemSelected(item);
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
        else if(id == R.id.nav_reports)
        {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.nav_stats)
        {
            Intent intent = new Intent(this, StatisticsActivity.class);
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

    private void createTasksList()
    {
        tasks = Task.getActiveTasks(0, ((CourierHelperApp)getApplication()).getCurrentUserEmail());
        if(tasks.size() != 0)
        {
            emptyMessage.setVisibility(View.GONE);
        }
        else
        {
            emptyMessage.setVisibility(View.VISIBLE);
        }
        tasksListViewAdapter = new TasksListViewAdapter(TasksActivity.this, R.layout.tasks_listview_row, tasks);
        tasksList.setAdapter(tasksListViewAdapter);
    }
}
