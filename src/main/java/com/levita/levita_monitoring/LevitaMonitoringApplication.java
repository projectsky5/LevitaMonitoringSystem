package com.levita.levita_monitoring;

import com.levita.levita_monitoring.integration.SheetsParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class LevitaMonitoringApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(LevitaMonitoringApplication.class, args);
		SheetsParser sheetParser = run.getBean(SheetsParser.class);
		sheetParser.getDataFromSheets();
	}

}
