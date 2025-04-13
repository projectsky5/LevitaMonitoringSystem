// TODO: Логирование
// TODO: Маппинг и отправку в БД через сервисный слой
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
import com.levita.levita_monitoring.service.KpiDataService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final List<SpreadsheetConfig> spreadsheetsConfig;
    private final KpiDataService kpiDataService;

    @Autowired
    public SheetsParser(List<SpreadsheetConfig> spreadsheetsConfig, KpiDataService kpiDataService) {
        this.spreadsheetsConfig = spreadsheetsConfig;
        this.kpiDataService = kpiDataService;
    }

    @PostConstruct
    public void init(){
        this.sheetsService = getSheetsService();
    }

    @Value("${application.name}")
    private static String applicationName;

    @Value("${google.sheets.credentials.file.path}")
    private String credentials;

    private Sheets sheetsService;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public void getDataFromSheets(){

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();

        for(SpreadsheetConfig config : spreadsheetsConfig) {
            final String spreadsheetId = config.getSpreadsheetId();
            final List<RangeDescriptor> ranges = config.getRanges();

            futures.add(executor.submit( () -> {
                try{
                    List<String> rangeStrings = ranges.stream()
                            .map(RangeDescriptor::range)
                            .toList();

                    BatchGetValuesResponse response = sheetsService.spreadsheets().values()
                            .batchGet(spreadsheetId)
                            .setRanges(rangeStrings)
                            .execute();

                    List<ValueRange> valueRanges = response.getValueRanges();

                    for (int i = 0; i < valueRanges.size(); i++) {
                        ValueRange valueRange = valueRanges.get(i);

                        List<List<Object>> values = valueRange.getValues();
                        if(values == null || values.isEmpty()) {
                            continue;
                        }

                        List<Object> row = values.get(0);
                        if(row.isEmpty()){
                            continue;
                        }

                        String value = row.get(0).toString();
                        RangeDescriptor descriptor = ranges.get(i);
                        kpiDataService.saveDataFromSheets(descriptor, value);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при пакетном запросе к таблице " + spreadsheetId, e);
                }
            } ));
        }

        for (Future<?> future : futures) {
            try{
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();


    }


    private void parseRanges(String spreadsheetId, List<RangeDescriptor> ranges){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();

        futures.add(executor.submit( () -> {
            try{
                List<String> rangeStrings = ranges.stream().map(RangeDescriptor::range).toList();

                BatchGetValuesResponse response = sheetsService.spreadsheets().values()
                        .batchGet(spreadsheetId)
                        .setRanges(rangeStrings)
                        .execute();

                List<ValueRange> valueRanges = response.getValueRanges();

                for (int i = 0; i < valueRanges.size(); i++) {
                    ValueRange valueRange = valueRanges.get(i);
                    List<List<Object>> values = valueRange.getValues();
                    if(values == null || values.isEmpty()) {
                        continue;
                    }

                    List<Object> row = values.get(0);
                    if(row.isEmpty()){
                        continue;
                    }

                    String value = row.get(0).toString();
                    RangeDescriptor descriptor = ranges.get(i);
                    kpiDataService.saveDataFromSheets(descriptor, value);
                }
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при пакетном запросе к таблице " + spreadsheetId, e);
            }
        }));

        for (Future<?> future : futures) {
            try{
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            executor.shutdown();
        }
    }

    public Sheets getSheetsService(){
        try{
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(credentials))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

            return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(applicationName)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Ошибка при создании SheetsService для " + credentials, e);
        }
    }
}
