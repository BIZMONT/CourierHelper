package com.bizmont.courierhelper.Task;

public class TaskDetails extends Task
{
    String content;
    String receiverName;
    String receiverPhone;
    String warehouseAddress;
    String date;
    String comment;
    String senderName;
    String senderAddress;
    String senderPhone;

    public TaskDetails(
            int id,
            TaskState state,
            String address,
            String content,
            String receiverName,
            String receiverPhone,
            String warehouseAddress,
            String date,
            String comment,
            String senderName,
            String senderAddress,
            String senderPhone)
    {
        super(id, state, address);

        this.content = content;
        this.receiverName = receiverName;
        this.warehouseAddress = warehouseAddress;
        this.date = date;
        this.comment = comment;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.senderPhone = senderPhone;
    }

    public TaskDetails(Task task, String content, String receiverName, String receiverPhone, String warehouseAddress,
                       String date, String comment, String senderName, String senderAddress,
                       String senderPhone)
    {
        this(task.getId(), task.getState(), task.getAddress(), content, receiverName, receiverPhone,
                warehouseAddress, date, comment, senderName, senderAddress, senderPhone);
    }

    public String getContent() {
        return content;
    }
    public String getReceiverName() {
        return receiverName;
    }
    public String getReceiverPhone() {
        return receiverPhone;
    }
    public String getWarehouseAddress() {
        return warehouseAddress;
    }
    public String getDate() {
        return date;
    }
    public String getComment() {
        return comment;
    }
    public String getSenderName() {
        return senderName;
    }
    public String getSenderAddress() {
        return senderAddress;
    }
    public String getSenderPhone() {
        return senderPhone;
    }

    @Override
    public String toString()
    {
        return "Task: " + this.id + "\n" +
                "State: " + this.state + "\n" +
                "Address: " + this.address + this.content + this.receiverName + this.receiverPhone;
    }
}
