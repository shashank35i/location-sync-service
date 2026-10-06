package com.goon.locationsync.service;

import com.goon.locationsync.service.request.LocationEvent;
import com.goon.locationsync.service.response.LocationUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumeLocationUpdates {

    private static final Duration LOC_TTL = Duration.ofHours(2);

    private final StringRedisTemplate redis;

    public void consume(LocationEvent e) {
        if (e.rideId() == null || e.rideId().isBlank()) {
            log.warn("Skipping location without rideId, driver {}", e.driverId());
            return;
        }

        byte[] geoKey  = ("loc:" + e.rideId()).getBytes(UTF_8);
        byte[] member  = e.driverId().getBytes(UTF_8);
        byte[] channel = ("ride:" + e.rideId() + ":loc").getBytes(UTF_8);
        byte[] payload = payload(e).getBytes(UTF_8);

        redis.executePipelined((RedisCallback<Object>) conn -> {
            conn.geoCommands().geoAdd(geoKey, new Point(e.lon(), e.lat()), member); // lon first
            conn.keyCommands().expire(geoKey, LOC_TTL.toSeconds());
            conn.execute("SPUBLISH", channel, payload);
            return null;
        });
    }

    private String payload(LocationEvent e) {
        return JsonMapper.shared().writeValueAsString(
                new LocationUpdate(e.driverId(), e.lat(), e.lon(), e.timestamp()));
    }
}