package com.bizmont.courierhelper.OtherStuff;

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
                return "Not Active";
        }
    }
}
