package com.goon.locationsync.service;

import com.goon.locationsync.service.request.LocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

        private final ConsumeLocationUpdates consumeLocationUpdates;
        private final ConsumeDriverUpdates consumeDriverUpdates;


    @KafkaListener(topics = "${app.kafka.topics.location-updates}")
    public void locationUpdatesConsumer(
            LocationEvent event,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info(
                "KAFKA START offset={}, timestamp={}",
                offset,
                event.timestamp()
        );

        consumeLocationUpdates.consume(event);

        log.info(
                "KAFKA SUCCESS offset={}, timestamp={}",
                offset,
                event.timestamp()
        );
    }

         @KafkaListener(topics = "${app.kafka.topics.driver-locations}")
         public void driverAvailabilityConsumer(LocationEvent locationEvent){
            consumeDriverUpdates.updateAvailableDriver(locationEvent);
        }


}
