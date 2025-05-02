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
    private String applicationName;

    @Value("${google.sheets.credentials.file.path}")
    private String credentials;

    private Sheets sheetsService;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void scheduleDataParsing(){
        log.info("Запуск планового парсинга данных из Google Sheets");
        getDataFromSheets();
        log.info("Плановый парсинг завершен");
    }

    private void getDataFromSheets(){
        parseLocations();
        parseUsers();
        parseKpiData();
    }

    private void parseLocations(){
        parseByCategory("LOCATIONS");
    }

    private void parseUsers(){
        parseByCategory("USERS");
    }

    private void parseKpiData(){
        List<String> skipCategories = List.of("USERS", "LOCATIONS");

        for (SpreadsheetConfig config : spreadsheetsConfig) {
            List<RangeDescriptor> filteredRanges = config.getRanges().stream()
                    .filter(descriptor -> !skipCategories.contains(descriptor.category()))
                    .toList();

            parseRanges(config.getSpreadsheetId(), filteredRanges);
        }
    }

    private void parseByCategory(String category){
        for (SpreadsheetConfig config : spreadsheetsConfig) {
            List<RangeDescriptor> filteredRanges = config.getRanges().stream()
                    .filter(descriptor -> descriptor.category().equalsIgnoreCase(category))
                    .toList();

            boolean hasSuchCategory = config.getRanges().stream()
                            .anyMatch(descriptor -> descriptor.category().equalsIgnoreCase(category));

            if(!filteredRanges.isEmpty()){
                parseRanges(config.getSpreadsheetId(), filteredRanges);
            }
        }
    }

    private void parseRanges(String spreadsheetId, List<RangeDescriptor> ranges){
        final int BATCH_SIZE = 30;

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < ranges.size(); i+=BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ranges.size());
            List<RangeDescriptor> batch = ranges.subList(i, end);

            futures.add(executor.submit( () -> {
                try{
                    List<String> rangeStrings = batch.stream()
                            .map(RangeDescriptor::range)
                            .toList();

                    log.info("Загрузка {} диапазонов из таблицы [{}]", batch.size(), spreadsheetId);

                    BatchGetValuesResponse response = sheetsService.spreadsheets().values()
                            .batchGet(spreadsheetId)
                            .setRanges(rangeStrings)
                            .execute();

                    List<ValueRange> valueRanges = response.getValueRanges();

                    if(valueRanges == null || valueRanges.size() != batch.size()){
                        log.warn("Ошибка: пустой или несоответствующий valueRanges из таблицы [{}]", spreadsheetId);
                        return;
                    }

                    for (int j = 0; j < valueRanges.size(); j++) {
                        List<List<Object>> values = valueRanges.get(j).getValues();
                        if(values == null || values.isEmpty()) {
                            log.warn("Пропущен пустой range [{}]", ranges.get(j).range());
                            continue;
                        }

                        List<Object> row = values.get(0);
                        if(row.isEmpty()){
                            log.warn("Пропущена строка в range [{}]", ranges.get(j).range());
                            continue;
                        }

                        String value = row.get(0).toString();
                        RangeDescriptor descriptor = batch.get(j);
                        kpiDataService.saveDataFromSheets(descriptor, value);
                    }
                } catch (IOException e) {
                    log.error("Ошибка при пакетном запросе к таблице [{}]", spreadsheetId, e);
                    throw new RuntimeException("Ошибка при пакетном запросе к таблице " + spreadsheetId, e);
                }
            }));
        }

        for (Future<?> future : futures) {
            try{
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Ошибка выполнения потока при чтении таблицы [{}]", spreadsheetId, e);
            }

            log.info("Завершена загрузка из таблицы [{}]", spreadsheetId);

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
            log.error("Ошибка при создании SheetsService для [{}]", credentials, e);
            throw new RuntimeException("Ошибка при создании SheetsService для " + credentials, e);
        }
    }
}
