package com.bizmont.courierhelper.Models.Report;

public class Report
{
    private int id;
    private int taskId;
    private String recommendedPath;
    private String trackPath;
    private long starTime;
    private long endTime;
    private String reason;

    public Report(int id, int taskId, String recommendedPath, String trackPath, long starTime, long endTime, String reason)
    {
        this.id = id;
        this.taskId = taskId;
        this.recommendedPath = recommendedPath;
        this.trackPath = trackPath;
        this.starTime = starTime;
        this.endTime = endTime;
        this.reason = reason;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getRecommendedPath() {
        return recommendedPath;
    }

    public String getTrackPath() {
        return trackPath;
    }

    public long getStarTime() {
        return starTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getReason() {
        return reason;
    }

    public int getID() {
        return id;
    }
}
