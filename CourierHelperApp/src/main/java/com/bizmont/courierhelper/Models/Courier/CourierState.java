package com.bizmont.courierhelper.Models.Courier;

public enum CourierState
{
    NOT_ACTIVE,
    AT_THE_POINT,
    IN_WAREHOUSE,
    ON_MOVE;

    @Override
    public String toString() {
        switch (this)
        {
            case NOT_ACTIVE:
                return "Not active";
            case ON_MOVE:
                return "On move";
            case AT_THE_POINT:
                return "At point of delivery";
            case IN_WAREHOUSE:
                return "In warehouse";
            default:
                return "Not active";
        }
    }
    public static CourierState parse(String state)
    {
        switch (state)
        {
            case "Not active":
                return NOT_ACTIVE;
            case "On move":
                return ON_MOVE;
            case "At point of delivery":
                return  AT_THE_POINT;
            case "In warehouse":
                return IN_WAREHOUSE;
            default:
                return NOT_ACTIVE;
        }
    }
}
