package com.bizmont.courierhelper.Task;

public class Task
{
    protected int id;
    protected TaskState state;
    protected String address;

    public Task(int id, TaskState state, String address)
    {
        this.id = id;
        this.state = state;
        this.address = address;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public int getId() {
        return id;
    }
}

