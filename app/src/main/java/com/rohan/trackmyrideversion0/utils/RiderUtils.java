package com.rohan.trackmyrideversion0.utils;

import com.rohan.trackmyrideversion0.pojos.Rider;

import java.util.List;

public final class RiderUtils {
    public RiderUtils() {
    }

    public static void updateOrders(List<Rider> riders) {
        for (int i = 0; i < riders.size(); i++) {
            Rider rider = riders.get(i);
            rider.orderNumber = i + 1;
        }
    }
}
