package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Report.ReportDetails;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.OtherStuff.RoadFile;
import com.bizmont.courierhelper.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReportDetailsActivity extends AppCompatActivity {
    private ReportDetails details;
    private Task task;

    private FolderOverlay recommendedPath;
    private FolderOverlay track;
    private MapView map;
    private IMapController mapController;

    private TextView taskId;
    private TextView startTime;
    private TextView endTime;
    private TextView pathDistance;
    private TextView recommendedDistance;

    private BoundingBoxE6 boxE6;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_details_activity);

        map = (MapView) findViewById(R.id.map);
        assert map != null;
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setMaxZoomLevel(18);
        map.setMinZoomLevel(12);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.setClickable(true);
        mapController = map.getController();

        Intent intent = getIntent();

        details = null;
        int id = intent.getIntExtra(ExtrasNames.REPORT_ID,0);

        boxE6 = null;
        details = DataBase.getReportDetails(id);
        if(details != null && id != 0)
        {
            task = DataBase.getTask(details.getTaskId());
            taskId = (TextView)findViewById (R.id.map_task_number);
            taskId.setText(getString(R.string.task_number) + details.getTaskId());

            startTime = (TextView)findViewById(R.id.report_begin_time);
            startTime.setText(simpleDateFormat.format(new Date(details.getStarTime())));
            endTime = (TextView)findViewById(R.id.report_end_time);
            endTime.setText(simpleDateFormat.format(new Date(details.getEndTime())));

            File file = new File(getFilesDir() + "/kml/recommended_paths", details.getTaskId() + "_rec.kml");
            recommendedPath = RoadFile.getOverlayFromFile(this, file, map);

            recommendedDistance = (TextView)findViewById(R.id.report_recommended_distance);
            GeoPoint taskCor = new GeoPoint(task.getLatitude(),task.getLongitude());
            recommendedDistance.setText(String.valueOf(calculateDistance(recommendedPath,taskCor)));

            boxE6 = RoadFile.getBoundingBox(this, file, map);

            file = new File(getFilesDir() + "/kml/tracks", String.valueOf(details.getTaskId()) + ".kml");
            track = RoadFile.getOverlayFromFile(this, file, map);

            pathDistance = (TextView)findViewById(R.id.report_traveled_distance);
            pathDistance.setText(String.valueOf(calculateDistance(track, null)));

            showMap();
        }

    }

    private void showMap()
    {
        map.getOverlays().add(track);
        map.getOverlays().add(recommendedPath);
        map.getOverlays().add(task.createMarker(this,map));
        map.getOverlays().add(task.getWarehouse().createMarker(this,map));
        if(boxE6 != null)
        {
            mapController.setZoom(14);
            mapController.setCenter(boxE6.getCenter());
            mapController.zoomToSpan(boxE6.getLatitudeSpanE6(),boxE6.getLongitudeSpanE6());
        }
        map.invalidate();
    }

    private double calculateDistance(FolderOverlay folderOverlay, GeoPoint stopLocation)
    {
        double length = 0;
        GeoPoint prevPoint;
        ArrayList<Polyline> lines = new ArrayList<>();

        for (Overlay path:folderOverlay.getItems())
        {
            lines.add((Polyline) path);
        }

        prevPoint = lines.get(0).getPoints().get(0);
        for (Polyline line: lines)
        {
            for (GeoPoint point : line.getPoints())
            {
                if(stopLocation != null &&
                        point.getLatitude() == stopLocation.getLatitude() &&
                        point.getLongitude() == point.getLongitude())
                {
                    return length;
                }
                length += point.distanceTo(prevPoint);
                prevPoint = point;
            }
        }
        return length;
    }

    public void onClickTaskDetails(View view)
    {
        Intent intent = new Intent(getApplicationContext(), TaskDetailsActivity.class);
        intent.putExtra(ExtrasNames.TASK_ID, task.getId());
        startActivity(intent);
    }
}
