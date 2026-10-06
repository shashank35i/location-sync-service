package com.goon.locationsync.service;

import com.goon.locationsync.service.request.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsumeDriverUpdates {

     private final StringRedisTemplate redis;
     private static final String AVAILABLE = "drivers:Available";
    private static final String LAST_SEEN = "drivers:last_seen";
    private static final long staleSeconds = 30;

    public void updateAvailableDriver(LocationEvent e) {
        if (Boolean.TRUE.equals(redis.hasKey("driver:busy:" + e.driverId()))) {
            return; // late ping from a driver who was just assigned
        }
        redis.opsForGeo().add(AVAILABLE, new Point(e.lon(), e.lat()), e.driverId());
        redis.opsForZSet().add(LAST_SEEN, e.driverId(), System.currentTimeMillis());
    }

    @Scheduled(fixedDelayString = "${app.driver.prune-interval-ms:60000}")
    public void pruneStaleDrivers() {
        double cutoff = (System.currentTimeMillis() * 1000L) - staleSeconds;
        System.out.println("scanned but nothing found");

        Set<String> stale = redis.opsForZSet().rangeByScore(LAST_SEEN, 0, cutoff);
        if (stale == null || stale.isEmpty()) return;
        String[] ids = stale.toArray(new String[0]);
        redis.opsForGeo().remove(AVAILABLE, ids);
        redis.opsForZSet().remove(LAST_SEEN, (Object[]) ids);
    }
}
