package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.service.report.RangeFilterService;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class SheetsParserService {

    private static final Logger log = LoggerFactory.getLogger(SheetsParserService.class);

    private final List<SpreadsheetConfig> spreadsheetsConfig;
    private final KpiDataService kpiDataService;
    private final SheetsClient sheetsClient;
    private final ExecutorService executor;
    private final RangeFilterService rangeFilter;

    private static final String USERS = "USERS";
    private static final String LOCATIONS = "LOCATIONS";
    private static final int BATCH_SIZE = 30;

    public SheetsParserService(List<SpreadsheetConfig> spreadsheetsConfig,
                               KpiDataService kpiDataService,
                               SheetsClient sheetsClient,
                               @Qualifier("parserExecutor") ExecutorService executor,
                               RangeFilterService rangeFilter) {
        this.spreadsheetsConfig = spreadsheetsConfig;
        this.kpiDataService = kpiDataService;
        this.sheetsClient = sheetsClient;
        this.executor = executor;
        this.rangeFilter = rangeFilter;
    }

    public void runSheetsParser() {
        log.info("Запуск планового парсинга данных из Google Sheets");
        parseLocations();
        parseUsers();
        parseKpiData();
        log.info("Плановый парсинг завершен");
    }

    void parseLocations() {
        for (SpreadsheetConfig config : spreadsheetsConfig) {
            List<RangeDescriptor> locRanges =
                    rangeFilter.filterByCategory(config, LOCATIONS);
            if (!locRanges.isEmpty()) {
                parseRanges(config.getSpreadsheetId(), locRanges);
            }
        }
    }

    void parseUsers () {
        for (SpreadsheetConfig config : spreadsheetsConfig) {
            List<RangeDescriptor> userRanges =
                    rangeFilter.filterByCategory(config, USERS);
            if (!userRanges.isEmpty()) {
                parseRanges(config.getSpreadsheetId(), userRanges);
            }
        }
    }

    void parseKpiData () {
        for (SpreadsheetConfig config : spreadsheetsConfig) {
            List<RangeDescriptor> filteredRanges =
                    rangeFilter.filterExcluding(config, List.of(LOCATIONS, USERS));
            if (!filteredRanges.isEmpty()) {
                parseRanges(config.getSpreadsheetId(), filteredRanges);
            }
        }
    }

    void processBatch(String spreadsheetId, List<RangeDescriptor> batch) throws IOException {
        List<String> rangeStrings = batch.stream()
                .map(RangeDescriptor::range)
                .toList();

        log.info("Загрузка {} диапазонов из таблицы [{}]", batch.size(), spreadsheetId);
        List<ValueRange> valueRanges = sheetsClient.batchGetValues(spreadsheetId, rangeStrings);

        if (valueRanges == null || valueRanges.size() != batch.size()) {
            log.warn("Ошибка: пустой или несоответствующий valueRanges из таблицы [{}]", spreadsheetId);
            return;
        }

        for (int i = 0; i < valueRanges.size(); i++) {
            List<List<Object>> values = valueRanges.get(i).getValues();
            if (values == null || values.isEmpty()) {
                log.warn("Пропущен пустой range [{}]", batch.get(i).range());
                continue;
            }
            List<Object> row = values.getFirst();
            if (row.isEmpty()) {
                log.warn("Пропущена строка в range [{}]", batch.get(i).range());
                continue;
            }
            String value = row.getFirst().toString();
            RangeDescriptor descriptor = batch.get(i);
            kpiDataService.saveDataFromSheets(descriptor, value);
        }
    }

    private void parseRanges (String spreadsheetId, List < RangeDescriptor > ranges){
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < ranges.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ranges.size());
            List<RangeDescriptor> batch = ranges.subList(i, end);
            futures.add(executor.submit(() -> {
                try {
                    processBatch(spreadsheetId, batch);
                } catch (IOException e) {
                    log.error("Ошибка при пакетном запросе к таблице [{}]", spreadsheetId, e);
                    throw new UncheckedIOException(e);
                }
            }));
        }
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Ошибка выполнения потока при чтении таблицы [{}]", spreadsheetId, e);
        }
    }
}