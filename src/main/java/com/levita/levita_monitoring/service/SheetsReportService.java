package com.levita.levita_monitoring.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.levita.levita_monitoring.configuration.sheet_reports.YamlConfigLoader;
import com.levita.levita_monitoring.configuration.sheet_reports.CurrentColumnsDescriptor;
import com.levita.levita_monitoring.configuration.sheet_reports.TrialColumnsDescriptor;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.dto.FullReportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SheetsReportService {

    private static final Logger log = LoggerFactory.getLogger(SheetsReportService.class);
    private static final String VALUE_INPUT_OPTION = "USER_ENTERED";
    private static final String EMPTY_CELL = "";
    private static final Pattern REPORT_DATE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2} - (пн|вт|ср|чт|пт|сб|вс)");

    private final Sheets sheets;
    private final YamlConfigLoader yamlLoader;

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd.MM - E", Locale.forLanguageTag("ru-RU"));

    public SheetsReportService(Sheets sheets,
                               YamlConfigLoader yamlLoader) {
        this.sheets = sheets;
        this.yamlLoader = yamlLoader;
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
        batch.add(buildShiftValueRange(dto.shift(), user, reportDate));
        batch.add(buildTrialValueRange(dto.trial(), user, reportDate));
        batch.add(buildCurrentValueRange(dto.current(), user, reportDate));

        try{
            if (!batch.isEmpty()) {
                sheets.spreadsheets().values()
                        .batchUpdate(spreadsheetId, new BatchUpdateValuesRequest()
                                .setValueInputOption(VALUE_INPUT_OPTION)
                                .setData(batch))
                        .execute();
            }
        } catch (GoogleJsonResponseException e) {
            log.error("Google API returned {}: {}",
                    e.getStatusCode(),
                    e.getDetails().toPrettyString()
            );
            throw e;
        }

        saveOperationsForDate(dto.operations(), user, reportDate);
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
                buildClearShiftValueRange(user, reportDate),
                buildClearTrialValueRange(user, reportDate),
                buildClearCurrentValueRange(user, reportDate)
        );
        sheets.spreadsheets().values()
                .batchUpdate(spreadsheetId, new BatchUpdateValuesRequest()
                        .setValueInputOption(VALUE_INPUT_OPTION)
                        .setData(clear))
                .execute();

        rollbackOperations(user, reportDate);
        log.info("Откат за {} завершён за {} мс", reportDate,
                Duration.between(start, Instant.now()).toMillis());
    }

    private ValueRange buildShiftValueRange(
            FullReportDto.ShiftReportDto dto,
            User user,
            String reportDate
    ) throws IOException {
        String location = user.getLocation().getName();
        String dateCol = yamlLoader.getShiftColumnsDescriptor().getShiftColumns().get(location);
        int row = findRowByDate(
                yamlLoader.getSheetNamesDescriptor().getShift(),
                dateCol,
                3,
                reportDate
        );
        List<Object> data = List.of(
                dto.shiftStart(),
                dto.shiftEnd(),
                user.getName()
        );
        String startCol = nextColumn(dateCol);
        String endCol = startCol;
        for (int i = 1; i < data.size(); i++) {
            endCol = nextColumn(endCol);
        }
        String range = String.format("'%s'!%s%d:%s%d",
                yamlLoader.getSheetNamesDescriptor().getShift(), startCol, row, endCol, row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    private ValueRange buildTrialValueRange(
            FullReportDto.TrialReportDto dto,
            User user,
            String reportDate
    ) throws IOException {
        String key = String.format("%s (%s)",
                user.getName(), user.getLocation().getName());
        TrialColumnsDescriptor.Entry entry =
                yamlLoader.getTrialColumnsDescriptor().getTrialColumns().get(key);
        String sheet = entry.getSheet();
        String[] cols = entry.getRange().split(":");
        int row = findRowByDate(sheet, "C", 8, reportDate);
        List<Object> data = List.of(
                dto.trialCame(), dto.trialBought(), dto.trialBoughtAmount(),
                dto.trialPaid(), dto.trialPaidAmount(), dto.prepayment(),
                dto.prepaymentAmount(), dto.surcharge(), dto.surchargeAmount()
        );
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    private ValueRange buildCurrentValueRange(
            FullReportDto.CurrentReportDto dto,
            User user,
            String reportDate
    ) throws IOException {
        String key = String.format("%s (%s)",
                user.getName(), user.getLocation().getName());
        CurrentColumnsDescriptor.Entry entry =
                yamlLoader.getCurrentColumnsDescriptor().getCurrentColumns().get(key);
        String sheet = entry.getSheet();
        String[] cols = entry.getRange().split(":");
        int row = findRowByDate(sheet, "A", 6, reportDate);
        List<Object> data = List.of(
                dto.finished(), dto.extended(), dto.extendedAmount(),
                dto.upgrades(), dto.upgradeAmount(), dto.returned(), dto.returnedAmount(),
                dto.prepayment(), dto.prepaymentAmount(), dto.surcharge(), dto.surchargeAmount(),
                dto.individual(), dto.individualAmount(), dto.singleVisits(), dto.singleVisitAmount()
        );
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    private void saveOperationsForDate(
            List<FullReportDto.OperationDto> operations,
            User user,
            String reportDate
    ) throws IOException {
        String location = user.getLocation().getName();
        String sheetName = yamlLoader.getSheetNamesDescriptor().getOperations().get(location);
        if (sheetName == null) {
            throw new IllegalArgumentException(
                    "Не найден лист операций для локации: " + location);
        }
        String dateCol = yamlLoader.getDateColumnsDescriptor().getDateColumns().get(sheetName);
        if (dateCol == null && sheetName.startsWith("Журнал операций")) {
            dateCol = "B";
        }
        int startRow = 2;
        int insertRow = findFirstEmptyRow(sheetName, dateCol, startRow);
        if (operations == null || operations.isEmpty()) {
            appendRow(List.of(reportDate), buildCellRange(sheetName, dateCol, insertRow));
            return;
        }
        try {
            for (FullReportDto.OperationDto dto : operations) {
                Object income;
                Object expense;

                if("Приход".equalsIgnoreCase(dto.type())){
                    income = formatAmount(dto.amount());
                    expense = "";
                } else {
                    income = "";
                    expense = formatAmount(dto.amount());
                }
                List<Object> row = Arrays.asList(
                        reportDate, income, expense,
                        dto.cashType(), dto.category(), user.getName(), dto.comment()
                );
                appendRow(row, buildCellRange(sheetName, dateCol, insertRow++));
            }
        } catch (GoogleJsonResponseException e) {
            log.error("Google API returned {}: {}",
                    e.getStatusCode(),
                    e.getDetails().toPrettyString()
            );
            throw e;
        }
    }

    private void rollbackOperations(User user, String reportDate) throws IOException {
        String location  = user.getLocation().getName();
        String sheetName = yamlLoader.getSheetNamesDescriptor()
                .getOperations()
                .get(location);
        if (sheetName == null) {
            throw new IllegalArgumentException("Не найден лист операций для локации: " + location);
        }

        // попытка получить колонку из date-columns.yml
        String dateCol = yamlLoader.getDateColumnsDescriptor()
                .getDateColumns()
                .get(sheetName);
        // если её нет и это лист операций — ставим «B»
        if (dateCol == null && sheetName.startsWith("Журнал операций")) {
            dateCol = "B";
        }

        int startRow = 2;
        List<Integer> rows = findRowsByDate(sheetName, dateCol, startRow, reportDate);
        if (rows.isEmpty()) {
            log.info("Откат операций: строк за дату [{}] не найдено в листе [{}]", reportDate, sheetName);
            return;
        }

        deleteRows(sheetName, rows);
        log.info("Откат операций: удалено {} строк за дату [{}] в листе [{}]", rows.size(), reportDate, sheetName);
    }

    private ValueRange buildClearShiftValueRange(User user, String reportDate) throws IOException {
        String dateCol = yamlLoader.getShiftColumnsDescriptor().getShiftColumns().get(user.getLocation().getName());
        int row = findRowByDate(yamlLoader.getSheetNamesDescriptor().getShift(), dateCol, 3, reportDate);
        String startCol = nextColumn(dateCol);
        String endCol = nextColumn(startCol);
        List<Object> empty = Collections.nCopies(
                columnToIndex(endCol) - columnToIndex(startCol) + 1, EMPTY_CELL);
        String range = String.format("'%s'!%s%d:%s%d",
                yamlLoader.getSheetNamesDescriptor().getShift(), startCol, row, endCol, row);
        return new ValueRange().setRange(range).setValues(List.of(empty));
    }

    private ValueRange buildClearTrialValueRange(User user, String reportDate) throws IOException {
        String key = String.format("%s (%s)",
                user.getName(), user.getLocation().getName());
        TrialColumnsDescriptor.Entry entry =
                yamlLoader.getTrialColumnsDescriptor().getTrialColumns().get(key);

        // вот тут исправляем
        String sheet = entry.getSheet();
        // берём кол-во колонки «Дата» из вашего dateColumns
        String dateCol = yamlLoader.getDateColumnsDescriptor().getDateColumns().get(sheet);
        // и уже по ней находим строку
        int row = findRowByDate(sheet, dateCol, 8, reportDate);

        String[] cols = entry.getRange().split(":");
        int count = columnToIndex(cols[1]) - columnToIndex(cols[0]) + 1;
        List<Object> empty = Collections.nCopies(count, EMPTY_CELL);
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range)
                .setValues(List.of(empty));
    }

    private ValueRange buildClearCurrentValueRange(User user, String reportDate) throws IOException {
        String key = String.format("%s (%s)",
                user.getName(), user.getLocation().getName());
        CurrentColumnsDescriptor.Entry entry =
                yamlLoader.getCurrentColumnsDescriptor().getCurrentColumns().get(key);

        String sheet = entry.getSheet();
        String dateCol = yamlLoader.getDateColumnsDescriptor().getDateColumns().get(sheet);
        int row = findRowByDate(sheet, dateCol, 6, reportDate);

        String[] cols = entry.getRange().split(":");
        int count = columnToIndex(cols[1]) - columnToIndex(cols[0]) + 1;
        List<Object> empty = Collections.nCopies(count, EMPTY_CELL);
        String range = String.format("'%s'!%s%d:%s%d",
                sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range)
                .setValues(List.of(empty));
    }

    // Общие утилиты:
    private int findRowByDate(String sheet, String column, int startRow, String date) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheet, column, startRow, column);
        List<List<Object>> rows = sheets.spreadsheets().values()
                .get(spreadsheetId, range).execute().getValues();
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (!rows.get(i).isEmpty() && date.equals(rows.get(i).getFirst().toString())) {
                    return startRow + i;
                }
            }
        }
        throw new IllegalArgumentException("Дата не найдена: " + date);
    }

    private List<Integer> findRowsByDate(String sheet, String column,
                                         int startRow, String date) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheet, column, startRow, column);
        List<List<Object>> rows = sheets.spreadsheets().values()
                .get(spreadsheetId, range).execute().getValues();
        List<Integer> result = new ArrayList<>();
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (!rows.get(i).isEmpty() && date.equals(rows.get(i).getFirst().toString())) {
                    result.add(startRow + i);
                }
            }
        }
        return result;
    }

    private int findFirstEmptyRow(String sheet, String column, int startRow) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheet, column, startRow, column);
        List<List<Object>> rows = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
                .getValues();

        // если API вернул null — значит нет ни одной заполненной строки в столбце,
        // первая свободная — startRow
        if (rows == null || rows.isEmpty()) {
            return startRow;
        }

        // ищем первый «пропуск» внутри rows
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).isEmpty()) {
                // rowIndex = startRow + i
                return startRow + i;
            }
        }

        // если до конца списка пустых не встретили — следующая за последней непустой
        return startRow + rows.size();
    }

    private void appendRow(List<Object> row, String range) throws IOException {
        sheets.spreadsheets().values()
                .append(spreadsheetId, range, new ValueRange().setValues(List.of(row)))
                .setValueInputOption(VALUE_INPUT_OPTION)
                .execute();
    }

    private void deleteRows(String sheetName, List<Integer> rows) throws IOException {
        Integer sheetId = getSheetIdByName(sheetName);
        rows.sort(Comparator.reverseOrder());
        List<Request> reqs = new ArrayList<>();
        for (Integer r : rows) {
            reqs.add(new Request().setDeleteDimension(
                    new DeleteDimensionRequest().setRange(
                            new DimensionRange()
                                    .setSheetId(sheetId)
                                    .setDimension("ROWS")
                                    .setStartIndex(r-1)
                                    .setEndIndex(r)
                    )
            ));
        }
        sheets.spreadsheets().batchUpdate(spreadsheetId,
                new BatchUpdateSpreadsheetRequest().setRequests(reqs)
        ).execute();
    }

    //    Метод получения идентификатора листа для DeleteDimensionRequest
    private Integer getSheetIdByName(String sheetName) throws IOException {
        return sheets.spreadsheets().get(spreadsheetId)
                .execute().getSheets().stream()
                .filter(s -> sheetName.equals(s.getProperties().getTitle()))
                .findFirst()
                .map(s -> s.getProperties().getSheetId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Идентификатор листа не найден: " + sheetName));
    }

    private String buildCellRange(String sheet, String col, int row) {
        return String.format("'%s'!%s%d", sheet, col, row);
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

    private Object formatAmount(BigDecimal amt) {
        if (amt == null) return null;
        BigDecimal clean = amt.stripTrailingZeros();
        return clean.scale() <= 0 ? clean.longValue() : clean.doubleValue();
    }

    private List<Object> buildOperationRow(String reportDate,
                                           Object income,
                                           Object expense,
                                           String cashType,
                                           String category,
                                           String admin,
                                           String comment
                                           ) {
        List<Object> row = new ArrayList<>(7);
        row.add(reportDate);
        row.add(income);
        row.add(expense);
        row.add(cashType);
        row.add(category);
        row.add(admin);
        row.add(comment);
        return row;
    }

    private List<Object> emptyRow(int size){
        return new ArrayList<>(Collections.nCopies(size, EMPTY_CELL));
    }

}
