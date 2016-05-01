package com.bizmont.courierhelper;

import android.content.Context;
import android.os.AsyncTask;

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

public class RouteBuilderTask extends AsyncTask<ArrayList<GeoPoint>,Void, Road>
{
    public static final String MAPQUEST_SERVICE = "http://open.mapquestapi.com/directions/v2/optimizedroute?";
    private static final String MAPQUEST_API_KEY = "w9zLBh8dLYSm5pM3iC579DspUgg29jur";

    @Override
    protected Road doInBackground(ArrayList<GeoPoint>... params)
    {
        ArrayList<GeoPoint> points = params[0];

        int[] sequence = getSequence(getRequestUrl(points));

        ArrayList<GeoPoint> sortedPoints = new ArrayList<>();

        for (int i = 0; i< sequence.length;i++)
        {
            sortedPoints.set(i, points.get(sequence[i]));
        }

        RoadManager roadManager = new MapQuestRoadManager(MAPQUEST_API_KEY);
        roadManager.addRequestOption("routeType=pedestrian");
        Road road = roadManager.getRoad(sortedPoints);

        return road;
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
        Pattern p = Pattern.compile("<locationSequence>(.*?)</locationSequence>");
        Matcher matcher = p.matcher(string);
        matcher.find();
        String[] sequenceString = matcher.group(1).split(",");
        int[] sequence = new int[sequenceString.length];
        for(int i = 0;i<sequence.length;i++)
        {
            sequence[i] = Integer.getInteger(sequenceString[i]);
        }
        return sequence;
    }
}
