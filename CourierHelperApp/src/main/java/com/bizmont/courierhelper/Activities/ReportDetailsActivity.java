package com.bizmont.courierhelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Report.ReportDetails;
import com.bizmont.courierhelper.Models.Task.Task;
import com.bizmont.courierhelper.OtherStuff.ExtrasNames;
import com.bizmont.courierhelper.OtherStuff.RoadFile;
import com.bizmont.courierhelper.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import java.io.File;

public class ReportDetailsActivity extends AppCompatActivity {
    ReportDetails details;
    Task task;

    FolderOverlay recommendedPath;
    FolderOverlay track;
    MapView map;
    IMapController mapController;

    TextView startTime;
    TextView endTime;

    BoundingBoxE6 boxE6;

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

        if(id != 0)
        {
            details = DataBase.getReportDetails(id);
            task = DataBase.getTask(details.getTaskId());
            setTitle(getString(R.string.task_number) + details.getTaskId());

            startTime = (TextView)findViewById(R.id.report_begin_time);
            startTime.setText(details.getStarTime());
            endTime = (TextView)findViewById(R.id.report_end_time);
            endTime.setText(details.getEndTime());
            File file = new File(getFilesDir() + "/kml/recommended_paths", details.getTaskId() + "_rec.kml");
            recommendedPath = RoadFile.getOverlayFromFile(this, file, map);
            boxE6 = RoadFile.getBoundingBox(this,file,map);
            file = new File(getFilesDir() + "/kml/tracks", String.valueOf(details.getTaskId()) + ".kml");
            track = RoadFile.getOverlayFromFile(this, file, map);
        }
        map.getOverlays().add(track);
        map.getOverlays().add(recommendedPath);
        map.getOverlays().add(task.createMarker(this,map));
        map.getOverlays().add(task.getWarehouse().createMarker(this,map));
        if(boxE6 != null)
        {
            Log.d("Report", "Zooming");
            mapController.setZoom(14);
            mapController.setCenter(boxE6.getCenter());
            mapController.zoomToSpan(boxE6.getLatitudeSpanE6(),boxE6.getLongitudeSpanE6());
            //map.zoomToBoundingBox(boxE6,true);
        }

        map.invalidate();
    }
}
