package com.levita.levita_monitoring.service.report;

import com.google.api.services.sheets.v4.model.*;
import com.levita.levita_monitoring.configuration.sheet_reports.CurrentColumnsDescriptor;
import com.levita.levita_monitoring.configuration.sheet_reports.TrialColumnsDescriptor;
import com.levita.levita_monitoring.configuration.sheet_reports.YamlConfigLoader;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class ReportValueRangeBuilderImpl implements ReportValueRangeBuilder {

    private static final Logger log = LoggerFactory.getLogger(ReportValueRangeBuilderImpl.class);

    private final YamlConfigLoader yamlLoader;
    private final SheetsClient sheetsClient;

    public ReportValueRangeBuilderImpl(YamlConfigLoader yamlLoader, SheetsClient sheetsClient) {
        this.yamlLoader = yamlLoader;
        this.sheetsClient = sheetsClient;
    }

    @Override
    public ValueRange buildShiftValueRange(FullReportDto.ShiftReportDto shift, User user, String reportDate) throws IOException {
        String normalizedLoc = normalizeLocation(user.getLocation().getName());
        String dateCol = yamlLoader.getShiftColumnsDescriptor().getShiftColumns().get(normalizedLoc);
        int row = findRowByDate(
                yamlLoader.getSheetNamesDescriptor().getShift(),
                dateCol,
                3,
                reportDate
        );
        log.info("2");
        List<Object> data = List.of(
                shift.shiftStart(),
                shift.shiftEnd(),
                user.getName()
        );
        log.info("3");
        String startCol = nextColumn(dateCol);
        String endCol = startCol;
        log.info("4");
        for (int i = 1; i < data.size(); i++) {
            endCol = nextColumn(endCol);
        }
        log.info("5");
        String range = String.format("'%s'!%s%d:%s%d",
                yamlLoader.getSheetNamesDescriptor().getShift(), startCol, row, endCol, row);
        log.info("6");
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    @Override
    public ValueRange buildTrialValueRange(FullReportDto.TrialReportDto trial, User user, String reportDate) throws IOException {
        String normalizedLoc = normalizeLocation(user.getLocation().getName());
        String key = String.format("%s (%s)",
                user.getName(), normalizedLoc);
        TrialColumnsDescriptor.Entry entry =
                yamlLoader.getTrialColumnsDescriptor().getTrialColumns().get(key);
        String sheet = entry.getSheet();
        String[] cols = entry.getRange().split(":");
        int row = findRowByDate(sheet, "C", 8, reportDate);
        List<Object> data = List.of(
                trial.trialCame(), trial.trialBought(), trial.trialBoughtAmount(),
                trial.trialPaid(), trial.trialPaidAmount(), trial.prepayment(),
                trial.prepaymentAmount(), trial.surcharge(), trial.surchargeAmount()
        );
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    @Override
    public ValueRange buildCurrentValueRange(FullReportDto.CurrentReportDto current, User user, String reportDate) throws IOException {
        String normalizedLoc = normalizeLocation(user.getLocation().getName());
        String key = String.format("%s (%s)",
                user.getName(), normalizedLoc);
        CurrentColumnsDescriptor.Entry entry =
                yamlLoader.getCurrentColumnsDescriptor().getCurrentColumns().get(key);
        String sheet = entry.getSheet();
        String[] cols = entry.getRange().split(":");
        int row = findRowByDate(sheet, "A", 6, reportDate);
        List<Object> data = List.of(
                current.finished(), current.extended(), current.extendedAmount(),
                current.upgrades(), current.upgradeAmount(), current.returned(), current.returnedAmount(),
                current.prepayment(), current.prepaymentAmount(), current.surcharge(), current.surchargeAmount(),
                current.individual(), current.individualAmount(), current.singleVisits(), current.singleVisitAmount()
        );
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    @Override
    public ValueRange buildClearShiftValueRange(User user, String reportDate) throws IOException {
        String normalizedLoc = normalizeLocation(user.getLocation().getName());
        String dateCol = yamlLoader.getShiftColumnsDescriptor().getShiftColumns().get(normalizedLoc);
        int row = findRowByDate(yamlLoader.getSheetNamesDescriptor().getShift(), dateCol, 3, reportDate);
        String startCol = nextColumn(dateCol);
        String endCol = nextColumn(startCol);
        List<Object> empty = Collections.nCopies(
                columnToIndex(endCol) - columnToIndex(startCol) + 1, "");
        String range = String.format("'%s'!%s%d:%s%d",
                yamlLoader.getSheetNamesDescriptor().getShift(), startCol, row, endCol, row);
        return new ValueRange().setRange(range).setValues(List.of(empty));
    }

    @Override
    public ValueRange buildClearTrialValueRange(User user, String reportDate) throws IOException {
        String normalizedLoc = normalizeLocation(user.getLocation().getName());
        String key = String.format("%s (%s)",
                user.getName(), normalizedLoc);
        TrialColumnsDescriptor.Entry trialEntry =
                yamlLoader.getTrialColumnsDescriptor().getTrialColumns().get(key);

        String sheet = trialEntry.getSheet();
        String[] cols = trialEntry.getRange().split(":");
        // берём кол-во колонки «Дата» из dateColumns
        String dateCol = yamlLoader.getDateColumnsDescriptor().getDateColumns().get(sheet);
        // и уже по ней находим строку
        int row = findRowByDate(sheet, dateCol, 8, reportDate);

        return buildClearRange(cols, sheet, row);
    }

    @Override
    public ValueRange buildClearCurrentValueRange(User user, String reportDate) throws IOException {
        String normalizedLoc = normalizeLocation(user.getLocation().getName());
        String key = String.format("%s (%s)",
                user.getName(), normalizedLoc);
        CurrentColumnsDescriptor.Entry currentEntry =
                yamlLoader.getCurrentColumnsDescriptor().getCurrentColumns().get(key);

        String sheet = currentEntry.getSheet();
        String[] cols = currentEntry.getRange().split(":");
        String dateCol = yamlLoader.getDateColumnsDescriptor().getDateColumns().get(sheet);
        int row = findRowByDate(sheet, dateCol, 6, reportDate);

        return buildClearRange(cols, sheet, row);
    }

    @Override
    public void updateDefaultValues(ValueRange range) throws IOException {
        sheetsClient.updateValues(range.getRange(), range.getValues());
    }

    private int findRowByDate(String sheet, String column, int startRow, String date) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheet, column, startRow, column);
        List<List<Object>> rows = sheetsClient.getValues(range);
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (!rows.get(i).isEmpty() && date.equals(rows.get(i).getFirst().toString())) {
                    return startRow + i;
                }
            }
        }
        throw new IllegalArgumentException("Дата не найдена: " + date);
    }

    private String nextColumn(String column) {
        char[] arr = column.toUpperCase().toCharArray();
        for (int i = arr.length - 1; i >= 0; i--) {
            if (arr[i] != 'Z') { arr[i]++; return new String(arr,0,i+1); }
            arr[i] = 'A';
        }
        return "A" + new String(arr);
    }

    private int columnToIndex(String col) {
        int res = 0;
        for (char c : col.toUpperCase().toCharArray()) {
            res = res * 26 + (c - 'A' + 1);
        }
        return res;
    }

    private List<Object> emptyRow(int size){
        return new ArrayList<>(Collections.nCopies(size, ""));
    }

    private ValueRange buildClearRange(String[] cols, String sheet, int row){
        int count = columnToIndex(cols[1]) - columnToIndex(cols[0]) + 1;
        List<Object> empty = emptyRow(count);
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range)
                .setValues(List.of(empty));
    }

    private String normalizeLocation(String rawLocation) {
        if (rawLocation == null || rawLocation.isBlank()) return rawLocation;
        // для русского локали
        String lower = rawLocation.toLowerCase(new Locale("ru"));
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

}
