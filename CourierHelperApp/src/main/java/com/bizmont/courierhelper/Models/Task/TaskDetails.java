package com.bizmont.courierhelper.Models.Task;

import com.bizmont.courierhelper.Models.TaskState;

public class TaskDetails extends Task
{
    protected int receiverId;
    protected int senderId;
    protected String content;
    protected String date;
    protected String comment;
    private String code;

    public TaskDetails(int id, String address, double latitude, double longitude, TaskState state,
                       int receiverId, int senderId, int warehouseId, String content, String date,
                       String comment, String code) {
        super(id, address, latitude, longitude, state, warehouseId);

        this.receiverId = receiverId;
        this.senderId = senderId;
        this.content = content;
        this.date = date;
        this.comment = comment;
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }
}
