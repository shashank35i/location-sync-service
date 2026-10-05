package com.goon.locationsync.service.request;



public record LocationEvent(String driverId, String rideId, double lat, double lon, long timestamp) {}
