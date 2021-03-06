package com.bizmont.courierhelper.Services;


import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.bizmont.courierhelper.CourierHelperApp;
import com.bizmont.courierhelper.CourierHelperFiles;
import com.bizmont.courierhelper.ExtrasNames;
import com.bizmont.courierhelper.Model.Courier.Courier;
import com.bizmont.courierhelper.Model.Courier.CourierState;
import com.bizmont.courierhelper.Model.Point;
import com.bizmont.courierhelper.Model.Report;
import com.bizmont.courierhelper.Model.Task.Task;
import com.bizmont.courierhelper.Model.Task.TaskState;
import com.bizmont.courierhelper.Model.Warehouse;
import com.bizmont.courierhelper.Notifications;

import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPSTracker extends Service implements LocationListener
{
    //constants
    public static final String BROADCAST_SEND_ACTION = "com.bizmont.courierhelper.coordinates"; //filter to map activity
    public static final String BROADCAST_RECEIVE_ACTION = "com.bizmont.courierhelper.actions"; //filter to service

    public static final int LOCATION_REFRESH_RATE = 0; //seconds
    public static final float LOCATION_REFRESH_DISTANCE = 5; //meters
    public static final float LOCATION_MIN_ACCURACY = 50;
    public static final int LOCATION_MIN_SATELLITES = 4;



    private static final String LOG_TAG = "GPS Tracker Service";

    //location fields
    private LocationManager locationManager;
    private Location lastFix;

    private boolean isLocationDisabled;

    private ArrayList<Point> points;
    private ArrayList<GeoPoint> track;
    private ArrayList<Polyline> route;

    private BroadcastReceiver broadcastReceiver;
    private Notifications notifications;

    private File recommendedRouteFile;
    private long beginTime;

    private String userEmail;

    @Override
    public void onCreate() {
        super.onCreate();

        route = new ArrayList<>();
        track = new ArrayList<>();
        recommendedRouteFile = new File(getFilesDir() + "/kml","recommended_path.kml");
        userEmail = ((CourierHelperApp)getApplication()).getCurrentUserEmail();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                boolean isUpdatePoints = intent.getBooleanExtra(ExtrasNames.IS_UPDATE_POINTS,false);
                boolean isCreateRoute = intent.getBooleanExtra(ExtrasNames.IS_CREATE_ROUTE,false);
                int completedTask = intent.getIntExtra(ExtrasNames.COMPLETE_TASK,0);

                if(isUpdatePoints)
                {
                    points = Point.getTargetPoints(userEmail);
                    isOnPoint(lastFix);
                    Log.d(LOG_TAG, "Point updated");
                    if(isOnTheWayExist(points) && isCreateRoute)
                    {
                        beginTime = (new Date()).getTime();
                    }
                }
                if (isCreateRoute)
                {
                    sendMessageBroadcast("Creating route...");
                    if(!isOnTheWayExist(points))
                    {
                        new Courier(userEmail).setState(CourierState.NOT_ACTIVE);
                        //DataBase.setCourierState(((CourierHelperApp)getApplication()).getCurrentUserEmail(), CourierState.NOT_ACTIVE);
                        recommendedRouteFile.delete();
                        track.clear();
                    }
                    else
                    {
                        ArrayList<Road> roads = buildOptimalRoute();
                        CourierHelperFiles.saveRoadsToFile(getApplicationContext(), recommendedRouteFile, roads);
                        for (Road road:roads)
                        {
                            route.add(RoadManager.buildRoadOverlay(road,getApplicationContext()));
                        }
                        Log.d(LOG_TAG, "Route created");
                    }
                    sendRouteBroadcast();
                }
                if(completedTask != 0)
                {
                    Log.d(LOG_TAG, "Complete task" + completedTask);
                    String reason = intent.getStringExtra(ExtrasNames.REASON);
                    completeTask(completedTask, reason);

                    points = Point.getTargetPoints(userEmail);
                    isOnPoint(lastFix);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_RECEIVE_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

        //Service status notification
        notifications = new Notifications(getApplicationContext());
        notifications.showServiceStatusNotify();

        //Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addNmeaListener(new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                }
            });
            locationManager.addGpsStatusListener(new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {

                }
            });
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * LOCATION_REFRESH_RATE, LOCATION_REFRESH_DISTANCE, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * LOCATION_REFRESH_RATE, LOCATION_REFRESH_DISTANCE, this);
        }

        //Last successful fix initialization
        lastFix = new Location(LocationManager.GPS_PROVIDER);
        lastFix.setLatitude(0);
        lastFix.setLongitude(0);

        points = Point.getTargetPoints(userEmail);
        if(recommendedRouteFile.exists())
        {
            route = convertOverlayToPolylineList(CourierHelperFiles.getOverlayFromFile(this,recommendedRouteFile,
                    new MapView(getApplicationContext())));
        }

        Log.d(LOG_TAG, "Service onCreate");
    }
    @Override
    public void onDestroy()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }

        notifications.hideLocationAlertNotify();
        notifications.hideServiceStateNotify();
        notifications.hideOnPointNotify();
        notifications.hidePathDivergenceNotify();

        unregisterReceiver(broadcastReceiver);

        Log.d(LOG_TAG, "Service onDestroy");
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(LOG_TAG, "Service onBind");
        sendLocationBroadcast(lastFix);
        isOnPoint(lastFix);
        return new Binder();
    }
    @Override
    public void onRebind(Intent intent)
    {
        Log.d(LOG_TAG, "Service onRebind");
        super.onRebind(intent);
        sendLocationBroadcast(lastFix);
        isOnPoint(lastFix);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG,"Service onUnbind");
        return true;
    }

    //LocationListener overridden methods
    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG,"onLocationChanged");
        getLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider)
    {
        notifications.hideLocationAlertNotify();
        isLocationDisabled = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation(locationManager.getLastKnownLocation(provider));
        }
    }
    @Override
    public void onProviderDisabled(String provider) {checkProvidersAvailability();}


    private void checkProvidersAvailability()
    {
        if(!isLocationDisabled) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                isLocationDisabled = true;
                notifications.showLocationAlertNotify();
            }
        }
    }
    private void getLocation(Location location)
    {
        if(location == null) {
            return;
        }

        Log.d(LOG_TAG,"Location received: " + location.getLatitude() + " " + location.getLongitude() + " " + getSatellitesInUse(locationManager));

        if(isLocationValid(location))
        {
            sendLocationBroadcast(location);
            if(isOnTheWayExist(points))
            {
                new Courier(userEmail).setState(CourierState.ON_MOVE);
                //DataBase.setCourierState(userEmail, CourierState.ON_MOVE);
                track.add(new GeoPoint(location.getLatitude(),location.getLongitude()));
                Log.d(LOG_TAG, "Added point(" + location.getLatitude() + ", " + location.getLongitude() + ") to track");
                Log.d(LOG_TAG, "Number of points: " + track.size());
                if(route != null && route.size() !=0)
                {
                    if(!isOnRoute(location, route))
                    {
                        Log.d(LOG_TAG,"Not on the route");
                        notifications.showPathDivergenceNotify();
                    }
                    else
                    {
                        notifications.hidePathDivergenceNotify();
                        Log.d(LOG_TAG,"On the route");
                    }
                }
            }
            else
            {
                new Courier(userEmail).setState(CourierState.NOT_ACTIVE);
                //DataBase.setCourierState(userEmail, CourierState.NOT_ACTIVE);
                recommendedRouteFile.delete();
                route.clear();
                track.clear();
                sendRouteBroadcast();
            }
            if(isOnPoint(location))
            {

            }
        }
    }
    private void sendLocationBroadcast(Location location)
    {
        Intent locationIntent = new Intent(BROADCAST_SEND_ACTION);

        locationIntent.putExtra(ExtrasNames.IS_LOCATION, true);
        locationIntent.putExtra(ExtrasNames.LOCATION, location);

        this.sendBroadcast(locationIntent);
        Log.d(LOG_TAG, "Location sent (" + location.getLatitude() + " " + location.getLongitude() + ")");
    }
    private void sendRouteBroadcast()
    {
        Intent pathIntent = new Intent(BROADCAST_SEND_ACTION);
        pathIntent.putExtra(ExtrasNames.IS_PATH_UPDATE, true);

        this.sendBroadcast(pathIntent);
        Log.d(LOG_TAG, "Path sent");
    }
    private void sendMessageBroadcast(String message)
    {
        Intent messageIntent = new Intent(BROADCAST_SEND_ACTION);
        messageIntent.putExtra(ExtrasNames.MESSAGE, message);

        this.sendBroadcast(messageIntent);
        Log.d(LOG_TAG, "Message was sent");
    }
    private boolean isLocationValid(Location location)
    {
        long currentTime = System.currentTimeMillis();
        int satellitesInUse = getSatellitesInUse(locationManager);
        NetworkLocationIgnorer networkLocationIgnorer = new NetworkLocationIgnorer();

        if(networkLocationIgnorer.shouldIgnore(location.getProvider(),currentTime))
        {
            Log.d(LOG_TAG, "Location was ignored: " + location.getLatitude() + " " + location.getLongitude());
            return false;
        }

        if (location.getAccuracy() < LOCATION_MIN_ACCURACY &&
                lastFix.distanceTo(location) > 7)
        {
            if(location.getProvider().equals("gps") &&  satellitesInUse < LOCATION_MIN_SATELLITES)
            {
                return false;
            }
            if(lastFix.getLatitude() != 0 && location.getSpeed() > 0.1)
            {
                return  false;
            }
            lastFix = location;
            Log.d(LOG_TAG,"Last fix changed to " + lastFix.getLatitude() + ", " + lastFix.getLongitude());
            return true;
        }
        return false;
    }
    private boolean isOnPoint(Location location)
    {
        GeoPoint lastFixGP = new GeoPoint(this.lastFix);
        GeoPoint currentGP;

        for(Point point : points)
        {
            currentGP = new GeoPoint(point.getLatitude(), point.getLongitude());
            int distance = lastFixGP.distanceTo(currentGP);

            if( distance <= point.getRadius() + location.getAccuracy())
            {
                if (notifications.isPointNotifyShows())
                {
                    return true;
                }
                if(point.getClass() == Warehouse.class)
                {
                    new Courier(userEmail).setState(CourierState.IN_WAREHOUSE);
                    //DataBase.setCourierState(userEmail, CourierState.IN_WAREHOUSE);
                    notifications.showWarehouseNotify(point);
                }
                else if(((Task)point).getState() != TaskState.IN_WAREHOUSE)
                {
                    new Courier(userEmail).setState(CourierState.AT_THE_POINT);
                    //DataBase.setCourierState(userEmail, CourierState.AT_THE_POINT);
                    notifications.showTargetNotify(point);
                }
                return true;
            }
        }

        notifications.hideOnPointNotify();
        return false;
    }
    private ArrayList<Road> buildOptimalRoute()
    {
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(new GeoPoint(lastFix));
        for (Point point: points)
        {
            if(point instanceof Task && ((Task)point).getState() == TaskState.ON_THE_WAY)
            {
                routePoints.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
            }
        }

        ArrayList<Road> roads = null;
        PathBuilderTask tr = new PathBuilderTask();
        tr.execute(routePoints);
        try
        {
            roads = tr.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return roads;
    }
    public void completeTask(int taskId, String reason)
    {
        if(createReport(taskId, reason))
        {
            Log.d(LOG_TAG, "Report created for task " + taskId);
            Task task = new Task(taskId);
            if(reason == null)
            {
                task.setState(TaskState.DELIVERED);
                //DataBase.setTaskState(TaskState.DELIVERED, taskId);
            }
            else
            {
                task.setState(TaskState.NOT_DELIVERED);
                //DataBase.setTaskState(TaskState.NOT_DELIVERED, taskId);
            }
        }
    }
    private boolean createReport(int taskId, String reason)
    {
        File recommended = new File(getFilesDir() + "/kml/recommended_paths", taskId + "_rec.kml");
        Date date = new Date();
        long endTime = date.getTime();
        String trackFilePath = null;

        try
        {
            File trackFile = new File(getFilesDir() + "/kml/tracks", String.valueOf(taskId) + ".kml");
            Log.d(LOG_TAG,"Number of points: " + track.size());

            Polyline trackLine = new Polyline(this);
            trackLine.setPoints(track);

            trackFilePath = CourierHelperFiles.saveTrackToFile(trackFile, trackLine);
            Log.d(LOG_TAG, "Track saved for task #" + taskId + " to " + trackFilePath);
            CourierHelperFiles.copyFile(recommendedRouteFile,recommended);
            Log.d(LOG_TAG, "Recommended path for task " + taskId + " saved to " + recommended.getAbsolutePath());
        }
        catch (IOException ex)
        {
            sendMessageBroadcast(ex.getMessage());
        }
        finally
        {
            Report.add(taskId,trackFilePath,recommended.getAbsolutePath(),beginTime,endTime,reason);
        }
        return true;
    }

    //static methods
    private static boolean isOnTheWayExist(ArrayList<Point> points)
    {
        for (Point point: points)
        {
            if(point instanceof Task && ((Task)point).getState() == TaskState.ON_THE_WAY)
            {
                return true;
            }
        }
        return false;
    }
    private static boolean isOnRoute(Location location, ArrayList<Polyline> lines)
    {
        double fallibility = 20.0e-5;
        double minDistance = Double.MAX_VALUE;

        GeoPoint locationGeoPoint = new GeoPoint(location);
        for (Polyline polyline:lines)
        {
            List<GeoPoint> points = polyline.getPoints();
            for(int i = 0; i < (points.size() - 1); i++)
            {
                double currentDistance = pointToSegmentDistance(points.get(i), points.get(i+1), locationGeoPoint);
                if(currentDistance < minDistance)
                {
                    minDistance = currentDistance;
                }
            }
        }
        return minDistance < fallibility;
    }
    private static ArrayList<Polyline> convertOverlayToPolylineList(FolderOverlay routeOverlay)
    {
        ArrayList<Polyline> lines = new ArrayList<>();

        for (Overlay path:routeOverlay.getItems())
        {
            lines.add((Polyline) path);
        }
        return lines;
    }
    private static double pointToSegmentDistance(GeoPoint aSegmentPoint, GeoPoint bSegmentPoint, GeoPoint p)
    {
        double distance;

        double px = p.getLatitude();
        double py = p.getLongitude();
        double ax = aSegmentPoint.getLatitude();
        double ay = aSegmentPoint.getLongitude();
        double bx = bSegmentPoint.getLatitude();
        double by = bSegmentPoint.getLongitude();

        double k = (by - ay)/(bx - ax);
        double b = ay - k * ax;
        double K = -1/k;
        double B = py - K * px;

        double dx = (B - b)/(k - K);
        double dy = K * dx + B;

        double dPA = Math.sqrt(Math.pow((px - ax), 2) + Math.pow((py - ay), 2));
        double dPB = Math.sqrt(Math.pow((px - bx), 2) + Math.pow((py - by), 2));

        double ab = Math.sqrt(Math.pow((ax - bx), 2) + Math.pow((ay - by), 2));
        double ad = Math.sqrt(Math.pow((ax - dx), 2) + Math.pow((ay - dy), 2));
        double db = Math.sqrt(Math.pow((dx - bx), 2) + Math.pow((dy - by), 2));

        if(Math.abs(ab - (ad + db)) > 1.0e-10)
        {
            distance = Math.min(dPA, dPB);
        }
        else
        {
            distance = Math.sqrt(Math.pow((px - dx), 2) + Math.pow((py - dy), 2));
        }
        return distance;
    }
    private static int getSatellitesInUse(LocationManager locationManager)
    {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        int inUse = 0;
        if (satellites != null) {
            for (GpsSatellite gpsSatellite : satellites) {
                if (gpsSatellite.usedInFix()) {
                    inUse++;
                }
            }
        }
        return inUse;
    }

}

class PathBuilderTask extends AsyncTask<ArrayList<GeoPoint>, Void, ArrayList<Road>>
{
    public static final String MAPQUEST_SERVICE = "http://open.mapquestapi.com/directions/v2/optimizedroute?";
    private static final String MAPQUEST_API_KEY = "w9zLBh8dLYSm5pM3iC579DspUgg29jur";

    @SafeVarargs
    @Override
    protected final ArrayList<Road> doInBackground(ArrayList<GeoPoint>... params)
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
            requestString.append("{latLng:{lat:").append(points.get(i).getLatitude()).append(",lng:").append(points.get(i).getLongitude()).append("}}");
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
            Log.d("PathBuilderTask", "Exception was thrown: " + ex.getMessage());
        }
        catch (Exception ex)
        {
            Log.d("PathBuilderTask", "Exception was thrown: " + ex.getMessage());
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
            Pattern p = Pattern.compile("<locationSequence>(.*?)</locationSequence>");
            matcher = p.matcher(string);
        if(matcher.find())
        {
            sequenceString = matcher.group(1).split(",");
        }
        else
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