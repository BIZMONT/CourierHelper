package com.bizmont.courierhelper;

public class Courier
{
    enum State
    {
        AT_THE_POINT_OF_DELIVERY,
        ON_THE_MOVE,
        IN_WAREHOUSE,
        NOT_INVOLVED
    }

    public static State state;
    public static String name;

    public Courier()
    {
        name = "Courier Name";
        state = State.NOT_INVOLVED;
    }

}
