package com.levita.levita_monitoring.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.service.AsyncParserService;
import com.levita.levita_monitoring.service.KpiDataService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.api.client.json.JsonFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class SheetsParser {

    private static final Logger log = LoggerFactory.getLogger(SheetsParser.class);

    private final AsyncParserService asyncParserService;

    public SheetsParser(AsyncParserService asyncParserService) {
        this.asyncParserService = asyncParserService;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void scheduleDataParsing(){
        asyncParserService.runParser();
    }


}
