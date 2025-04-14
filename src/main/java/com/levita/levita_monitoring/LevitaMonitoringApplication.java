package com.levita.levita_monitoring;

import com.levita.levita_monitoring.integration.SheetsParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LevitaMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(LevitaMonitoringApplication.class, args);
	}

}
