package com.goon.locationsync.service;

import com.goon.locationsync.service.request.LocationEvent;
import com.goon.locationsync.service.response.LocationUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumeLocationUpdates {

    private final StringRedisTemplate redis;

        public void consume(LocationEvent e){
            if(e.rideId()==null || e.rideId().isBlank()){
                log.warn("not a rde");
            }

            String geoKey = "loc" + e.rideId();
            redis.opsForGeo().add(geoKey,new Point(e.lat(),e.lon()),e.driverId());
            redis.expire(geoKey, Duration.ofHours(2));
            redis.convertAndSend("ride:"+ e.rideId() + ":loc",payload(e));
        }

    private String payload(LocationEvent e) {
      return JsonMapper.shared().writeValueAsString(new LocationUpdate(e.driverId(),e.lat(),e.lon(),e.timestamp()));
        }
}
