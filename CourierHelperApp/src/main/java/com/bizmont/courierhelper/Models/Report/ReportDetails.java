package com.bizmont.courierhelper.Models.Report;

public class ReportDetails extends Report {
    public ReportDetails(int Id, int taskId, String recommendedPath, String trackPath, long starTime, long endTime, String reason) {
        super(Id, taskId, recommendedPath, trackPath, starTime, endTime, reason);
    }
}
