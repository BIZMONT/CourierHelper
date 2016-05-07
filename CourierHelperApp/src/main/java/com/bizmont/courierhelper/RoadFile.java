package com.bizmont.courierhelper;


import android.content.Context;
import android.graphics.Color;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;

public final class RoadFile
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
    public static String saveRecommendedPathToFile(Context context, File file, ArrayList<Road> path)
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
            pathPart.setColor(Color.GRAY);
            kmlDocument.mKmlRoot.addOverlay(pathPart,kmlDocument);
        }
        kmlDocument.saveAsKML(file);
        return file.getAbsolutePath();
    }
    public static String saveTrackToFile(File file, Polyline track)
    {
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.mKmlRoot.addOverlay(track,kmlDocument);
        kmlDocument.saveAsKML(file);
        return file.getAbsolutePath();
    }
}
