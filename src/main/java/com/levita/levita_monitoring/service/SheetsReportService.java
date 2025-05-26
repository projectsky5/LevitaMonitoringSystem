package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.model.*;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.service.report.OperationsService;
import com.levita.levita_monitoring.service.report.ReportValueRangeBuilder;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SheetsReportService {

    private static final Logger log = LoggerFactory.getLogger(SheetsReportService.class);
    private static final Pattern REPORT_DATE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2} - (пн|вт|ср|чт|пт|сб|вс)");

    private final SheetsClient sheetsClient;
    private final ReportValueRangeBuilder builder;
    private final OperationsService operationsService;

    public SheetsReportService(SheetsClient sheetsClient,
                               ReportValueRangeBuilder builder,
                               OperationsService operationsService) {
        this.sheetsClient = sheetsClient;
        this.builder = builder;
        this.operationsService = operationsService;
    }

    public void updateFullReport(User user, FullReportDto dto) throws IOException {
        String reportDate = dto.reportDate();
        if (!REPORT_DATE_PATTERN.matcher(reportDate).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Неправильный формат reportDate: " + reportDate);
        }
        log.info("Начало загрузки отчёта за {} (пользователь {})", reportDate, user.getName());
        Instant start = Instant.now();

        List<ValueRange> batch = new ArrayList<>();
        batch.add(builder.buildShiftValueRange(dto.shift(), user, reportDate));
        batch.add(builder.buildTrialValueRange(dto.trial(), user, reportDate));
        batch.add(builder.buildCurrentValueRange(dto.current(), user, reportDate));

        for (ValueRange valueRange : batch) {
            sheetsClient.updateValues(valueRange.getRange(), valueRange.getValues());
        }

        if (!batch.isEmpty()) {
            for (ValueRange valueRange : batch) {
                builder.updateDefaultValues(valueRange);
            }
        }

        operationsService.saveOperations(dto.operations(), user, reportDate);
        log.info("Загрузка за {} завершена за {} мс", reportDate,
                Duration.between(start, Instant.now()).toMillis());
    }

    public void rollbackFullReport(User user, String reportDate) throws IOException {
        if (!REPORT_DATE_PATTERN.matcher(reportDate).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Неправильный формат reportDate: " + reportDate);
        }
        log.info("Начало отката отчёта за {} (пользователь {})", reportDate, user.getName());
        Instant start = Instant.now();

        // очистка смен, проб, текущих
        List<ValueRange> clear = List.of(
                builder.buildClearShiftValueRange(user, reportDate),
                builder.buildClearTrialValueRange(user, reportDate),
                builder.buildClearCurrentValueRange(user, reportDate)
        );
        for (ValueRange valueRange : clear) {
            sheetsClient.updateValues(valueRange.getRange(), valueRange.getValues());
        }

        operationsService.rollbackOperations(user, reportDate);
        log.info("Откат за {} завершён за {} мс", reportDate,
                Duration.between(start, Instant.now()).toMillis());
    }

}
