package com.bizmont.courierhelper.Models.Task;

public enum TaskState
{
    DELIVERED,
    NOT_DELIVERED,
    IN_WAREHOUSE,
    ON_THE_WAY;

    @Override
    public String toString() {
        switch (this)
        {
            case DELIVERED:
                return "Delivered";
            case ON_THE_WAY:
                return "On the way";
            case IN_WAREHOUSE:
                return "In warehouse";
            case NOT_DELIVERED:
                return "Not delivered";
            default:
                return "Unknown";
        }
    }

    public static TaskState Parse(String state)
    {
        switch (state)
        {
            case "Delivered":
                return DELIVERED;
            case "On the way":
                return ON_THE_WAY;
            case "In warehouse":
                return IN_WAREHOUSE;
            case "Not delivered":
                return NOT_DELIVERED;
            default:
                throw new IllegalArgumentException();
        }
    }
}
