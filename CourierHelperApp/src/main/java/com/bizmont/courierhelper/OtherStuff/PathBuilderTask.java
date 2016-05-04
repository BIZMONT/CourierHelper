package com.bizmont.courierhelper.OtherStuff;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bizmont.courierhelper.Point.Point;
import com.squareup.okhttp.Route;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathBuilderTask extends AsyncTask<ArrayList<GeoPoint>,Void, ArrayList<Road>>
{
    public static final String MAPQUEST_SERVICE = "http://open.mapquestapi.com/directions/v2/optimizedroute?";
    private static final String MAPQUEST_API_KEY = "w9zLBh8dLYSm5pM3iC579DspUgg29jur";

    private final static String LOG_TAG = "RouteBuilder";

    @Override
    protected ArrayList<Road> doInBackground(ArrayList<GeoPoint>... params)
    {
        ArrayList<GeoPoint> points = params[0];

        int[] sequence = getSequence(getRequestUrl(points));
        if(sequence == null)
        {
            return null;
        }

        ArrayList<GeoPoint> sortedPoints = new ArrayList<>();

        for (int i = 0; i< sequence.length;i++)
        {
            sortedPoints.add(i, points.get(sequence[i]));
        }

        RoadManager roadManager = new MapQuestRoadManager(MAPQUEST_API_KEY);
        roadManager.addRequestOption("routeType=pedestrian");

        ArrayList<Road> roads = new ArrayList<>();
        for (int i = 0; i<sortedPoints.size()-1;i++)
        {
            ArrayList<GeoPoint> startEnd = new ArrayList<>();
            startEnd.add(sortedPoints.get(i));
            startEnd.add(sortedPoints.get(i+1));
            Road road = roadManager.getRoad(startEnd);
            roads.add(road);
        }

        return roads;
    }
    private String getRequestUrl(ArrayList<GeoPoint> points)
    {
        StringBuilder requestString = new StringBuilder(MAPQUEST_SERVICE);
        requestString.append("key=" + MAPQUEST_API_KEY);
        requestString.append("&json={locations:[");
        for (int i = 0; i < points.size(); i++)
        {
            requestString.append("{latLng:{lat:" + points.get(i).getLatitude() + ",lng:" + points.get(i).getLongitude() + "}}");
            if (i != points.size() - 1) {
                requestString.append(",");
            }
        }
        requestString.append("]}");
        requestString.append("&outFormat=xml");
        requestString.append("&shapeFormat=cmp");
        requestString.append("&narrativeType=text");
        requestString.append("&units=k&fishbone=false");
        requestString.append("&routeType=pedestrian");

        return requestString.toString();
    }
    private int[] getSequence(String requestUrl)
    {
        HttpConnection connection = new HttpConnection();
        StringBuilder responseString = new StringBuilder();
        try
        {
            connection.doGet(requestUrl);
            InputStream stream = connection.getStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while((line = reader.readLine()) != null) {
                responseString.append(line);
            }
        }
        catch (IOException ex)
        {

        }
        finally
        {
            connection.close();
        }
        return parseSequence(responseString.toString());
    }
    public static int[] parseSequence(String string)
    {
        Matcher matcher;
        String[] sequenceString;
        try {
            Pattern p = Pattern.compile("<locationSequence>(.*?)</locationSequence>");
            matcher = p.matcher(string);
            matcher.find();
            sequenceString = matcher.group(1).split(",");
        }
        catch (Exception ex)
        {
            return null;
        }

        int[] sequence = new int[sequenceString.length];
        for(int i = 0; i < sequence.length; i++)
        {
            sequence[i] = Integer.parseInt(sequenceString[i]);
        }
        return sequence;
    }
}
