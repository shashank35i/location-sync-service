package com.goon.locationsync.service;

import com.goon.locationsync.service.request.LocationEvent;
import com.goon.locationsync.service.response.LocationUpdate;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.annotation.PreDestroy;
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
public class ConsumeLocationUpdates {

    private static final Duration LOC_TTL = Duration.ofHours(2);

    private final StringRedisTemplate redis;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;

    public ConsumeLocationUpdates(StringRedisTemplate redis) {
        this.redis = redis;
        this.redisClient = RedisClient.create("redis://localhost:6379");
        this.connection = redisClient.connect();
    }

    public void consume(LocationEvent e) {

        if (e.rideId() == null || e.rideId().isBlank()) {
            log.warn("Skipping location without rideId");
            return;
        }

        String geoKey = "loc:" + e.rideId();
        String channel = "ride:" + e.rideId() + ":loc";

        redis.executePipelined((RedisCallback<Object>) conn -> {
            conn.geoCommands().geoAdd(
                    geoKey.getBytes(UTF_8),
                    new Point(e.lon(), e.lat()),
                    e.driverId().getBytes(UTF_8)
            );

            conn.keyCommands().expire(
                    geoKey.getBytes(UTF_8),
                    LOC_TTL.toSeconds()
            );

            return null;
        });

        String payload = JsonMapper.shared().writeValueAsString(
                new LocationUpdate(
                        e.driverId(),
                        e.lat(),
                        e.lon(),
                        e.timestamp()
                )
        );

        Long receivers = connection.sync().spublish(channel, payload);

        log.info(
                "Published ride={}, timestamp={}, receivers={}",
                e.rideId(),
                e.timestamp(),
                receivers
        );
    }

    @PreDestroy
    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}