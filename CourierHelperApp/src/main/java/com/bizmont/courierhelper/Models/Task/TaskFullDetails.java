package com.bizmont.courierhelper.Models.Task;

import com.bizmont.courierhelper.DataBase.DataBase;
import com.bizmont.courierhelper.Models.Receiver.Receiver;
import com.bizmont.courierhelper.Models.Sender.Sender;
import com.bizmont.courierhelper.Models.TaskState;

public class TaskFullDetails extends TaskDetails
{
    Receiver receiver;
    Sender sender;

    public TaskFullDetails(int id, String address, double latitude, double longitude,
                           TaskState state, int receiverId, int senderId, int warehouseId,
                           String content, String date, String comment, String code)
    {
        super(id, address, latitude, longitude, state, receiverId, senderId, warehouseId, content,
                date, comment, code);

        sender = DataBase.getSender(senderId);
        receiver = DataBase.getReceiver(receiverId);
    }

    public String getReceiverName() {
        return receiver.getName();
    }
    public String getReceiverPhone() {
        return receiver.getPhone();
    }
    public String getWarehouseAddress() {
        return warehouse.getAddress();
    }
    public String getSenderName() {
        return sender.getName();
    }
    public String getSenderAddress() {
        return sender.getAddress();
    }
    public String getSenderPhone() {
        return sender.getPhone();
    }
}
