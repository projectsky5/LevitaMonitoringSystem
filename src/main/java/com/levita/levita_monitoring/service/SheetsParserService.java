package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class SheetsParserService {

    private static final Logger log = LoggerFactory.getLogger(SheetsParserService.class);

    private final List<SpreadsheetConfig> spreadsheetsConfig;
    private final KpiDataService kpiDataService;
    private final Sheets sheetsService;

    @Value("${sheets.thread-pool-size}")
    private int threadPoolSize;

    public SheetsParserService(List<SpreadsheetConfig> spreadsheetsConfig,
                            KpiDataService kpiDataService,
                            Sheets sheetsService) {
        this.spreadsheetsConfig = spreadsheetsConfig;
        this.kpiDataService = kpiDataService;
        this.sheetsService = sheetsService;
    }

    public void runSheetsParser() {
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

            if(!filteredRanges.isEmpty()){
                parseRanges(config.getSpreadsheetId(), filteredRanges);
            }
        }
    }

    private void parseRanges(String spreadsheetId, List<RangeDescriptor> ranges){
        final int BATCH_SIZE = 30;

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
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

                        List<Object> row = values.getFirst();
                        if(row.isEmpty()){
                            log.warn("Пропущена строка в range [{}]", ranges.get(j).range());
                            continue;
                        }

                        String value = row.getFirst().toString();
                        RangeDescriptor descriptor = batch.get(j);
                        kpiDataService.saveDataFromSheets(descriptor, value);
                    }
                } catch (IOException e) {
                    log.error("Ошибка при пакетном запросе к таблице [{}]", spreadsheetId, e);
                    throw new RuntimeException("Ошибка при пакетном запросе к таблице " + spreadsheetId, e);
                }
            }));
        }

        try{
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Ошибка выполнения потока при чтении таблицы [{}]", spreadsheetId, e);
        } finally {
            executor.shutdown();
        }
    }

}
