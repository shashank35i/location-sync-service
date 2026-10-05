package com.goon.locationsync;

import com.goon.locationsync.service.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@EnableScheduling
public class LocationSyncServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocationSyncServiceApplication.class, args);
	}

}
