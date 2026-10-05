package com.goon.locationsync.service.response;

public record LocationUpdate(
        String driverId,   //for driver profile,stats and metrics
        double lat,
        double lon,
        long timestamp
) { }
