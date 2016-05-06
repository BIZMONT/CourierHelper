package com.bizmont.courierhelper.Models.Report;

/**
 * Created by bizmo on 06-May-16.
 */
public class ReportDetails extends Report {
    public ReportDetails(int Id, int taskId, String recommendedPath, String trackPath, String starTime, String endTime, String reason) {
        super(Id, taskId, recommendedPath, trackPath, starTime, endTime, reason);
    }
}
