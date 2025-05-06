package com.levita.levita_monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan("com.levita.levita_monitoring.configuration.sheet_reports")
@EnableScheduling
@EnableAsync
public class LevitaMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(LevitaMonitoringApplication.class, args);
	}

}
