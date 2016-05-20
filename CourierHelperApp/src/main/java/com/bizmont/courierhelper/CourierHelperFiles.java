package com.bizmont.courierhelper;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public final class CourierHelperFiles
{
    public static FolderOverlay getOverlayFromFile(Context context, File file, MapView mapView)
    {
        KmlDocument kmlDocument = new KmlDocument();
        if(file.exists())
        {
            kmlDocument.parseKMLFile(file);
            return (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mapView,null,null,kmlDocument);
        }
        return new FolderOverlay(context);
    }
    public static String saveRoadsToFile(Context context, File file, ArrayList<Road> path)
    {
        if(path == null)
        {
            return null;
        }
        KmlDocument kmlDocument = new KmlDocument();
        if(file.exists())
        {
            file.delete();
        }
        for (Road road:path)
        {
            Polyline pathPart = RoadManager.buildRoadOverlay(road, context);
            pathPart.setColor(ContextCompat.getColor(context, R.color.on_the_way));
            pathPart.setWidth(5);
            kmlDocument.mKmlRoot.addOverlay(pathPart,kmlDocument);
        }
        kmlDocument.saveAsKML(file);
        return file.getAbsolutePath();
    }
    public static String saveTrackToFile(File file, Polyline track)
    {
        Log.d("File road", "Saving track to file");
        for (GeoPoint point:track.getPoints()) {
            Log.d("File road", "Point: " + point.getLatitude() + ", " + point.getLongitude());
        }
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.mKmlRoot.addOverlay(track,kmlDocument);
        kmlDocument.saveAsKML(file);
        return file.getAbsolutePath();
    }
    public static void copyFile(File src, File dst) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    public static BoundingBoxE6 getBoundingBox(Context context, File file, MapView mapView)
    {
        KmlDocument kmlDocument = new KmlDocument();
        if(file.exists())
        {
            kmlDocument.parseKMLFile(file);
            return kmlDocument.mKmlRoot.getBoundingBox();
        }
        return null;
    }
}
