package com.bizmont.courierhelper.OtherStuff;

public class Report extends Task
{
    public Report(int id, String name,TaskState state){
        super(id,state,name);
        this.id = id;
        this.address = name;
        this.state = state;
    }
}
