package com.goon.locationsync.service;

import com.goon.locationsync.service.request.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncService {

        private final ConsumeLocationUpdates consumeLocationUpdates;
        private final ConsumeDriverUpdates consumeDriverUpdates;


        @KafkaListener(topics = "${app.kafka.topics.location-updates}")
        public void locationUpdatesConsumer(LocationEvent locationEvent){
                consumeLocationUpdates.consume(locationEvent);
        }

         @KafkaListener(topics = "${app.kafka.topics.driver-locations}")
         public void driverAvailabilityConsumer(LocationEvent locationEvent){
            consumeDriverUpdates.updateAvailableDriver(locationEvent);
        }


}
