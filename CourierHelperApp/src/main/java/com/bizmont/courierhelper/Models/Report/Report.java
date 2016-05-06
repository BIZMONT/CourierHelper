package com.bizmont.courierhelper.Models.Report;

public class Report
{
    private int ID;
    private int taskId;
    private String recommendedPath;
    private String trackPath;
    private String starTime;
    private String endTime;
    private String reason;

    public Report(int Id, int taskId, String recommendedPath, String trackPath, String starTime, String endTime, String reason)
    {
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

    public String getStarTime() {
        return starTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getReason() {
        return reason;
    }

    public int getID() {
        return ID;
    }
}
