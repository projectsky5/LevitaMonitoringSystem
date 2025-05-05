package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.sheet_reports.*;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SheetsReportService {

    private static final Logger log = LoggerFactory.getLogger(SheetsReportService.class);

    private static final String VALUE_INPUT_OPTION = "USER-ENTERED";
    private static final String EMPTY_CELL = "";

    private final Sheets sheets;
    private final ShiftColumnsConfig shiftColumnsConfig;
    private final SheetNamesConfig sheetNamesConfig;
    private final CurrentColumnsConfig currentColumnsConfig;
    private final DateColumnsConfig dateColumnsConfig;
    private final TrialColumnsConfig trialColumnsConfig;

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM - E", Locale.forLanguageTag("ru-RU"));

    public SheetsReportService(Sheets sheets,
                               ShiftColumnsConfig shiftColumnsConfig,
                               SheetNamesConfig sheetNamesConfig,
                               CurrentColumnsConfig currentColumnsConfig,
                               DateColumnsConfig dateColumnsConfig,
                               TrialColumnsConfig trialColumnsConfig) {
        this.sheets = sheets;
        this.shiftColumnsConfig = shiftColumnsConfig;
        this.sheetNamesConfig = sheetNamesConfig;
        this.currentColumnsConfig = currentColumnsConfig;
        this.dateColumnsConfig = dateColumnsConfig;
        this.trialColumnsConfig = trialColumnsConfig;
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void insertDateRowIfMissing() throws IOException {
        String today = LocalDate.now().format(dateFormatter);
        log.info("{} - Запуск плановой установки текущей даты", today);

        for(Map.Entry<String, String> entry : dateColumnsConfig.getColumns().entrySet()){
            String sheetName = entry.getKey();
            if (sheetName.startsWith("Журнал операций")){ // TODO: config
                log.info("Пропущена плановая вставка даты в раздел \"Журнал операций\" для листа: [{}]", sheetName);
                continue;
            }

            String col = entry.getValue();

            int startRow = switch (sheetName){
                case "Пробные (команда Кати)", "Пробные (команда Алины)" -> 8; // TODO: config
                case "Текущие (команда Кати)", "Текущие (команда Алины)" -> 6; // TODO: config
                default -> 3;
            };

            String range = String.format("'%s'!%s%d:%s", sheetName, col, startRow, col);

            ValueRange response = sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values != null) {
                boolean alreadyExists = values.stream()
                        .anyMatch(row -> !row.isEmpty() && today.equals(row.getFirst().toString()));

                if(!alreadyExists) {
                    int rowToInsert = startRow + values.size();
                    String insertRange = buildCellRange(sheetName, col, rowToInsert);

                    ValueRange body = new ValueRange().setValues(List.of(List.of(today)));

                    sheets.spreadsheets().values()
                            .update(spreadsheetId, insertRange, body)
                            .setValueInputOption("USER-ENTERED")
                            .execute();
                }
            }
            log.info("Дата в листе [{}] установлена успешно", sheetName);
        }
        log.info("{} - Плановая установка текущей даты завершена", today);
    }

    public void updateFullReport(FullReportDto dto, User user) throws IOException {
        String today = LocalDate.now().format(dateFormatter);
        log.info("{} - Начало загрузки данных", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Instant start = Instant.now();
        updateShiftReport(dto.shift(), user, today);
        updateTrialReport(dto.trial(), user, today);
        updateCurrentReport(dto.current(), user, today);
        saveOperationsForDate(dto.operations(), user, today);
        Instant end = Instant.now();

        Duration duration = Duration.between(start, end);
        log.info("Загрузка данных за дату {} завершена за {} мс", today, duration.toMillis());
    }

    private void saveOperationsForDate(List<FullReportDto.OperationDto> operations, User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String sheetName = sheetNamesConfig.getOperations().get(location);
        if (sheetName == null) {
            log.warn("Не найден лист для операций по локации: [{}]", location);
            throw new IllegalArgumentException("Не найден лист для операций по локации: " + location);
        }

        String dateCol = dateColumnsConfig.getColumns().get(sheetName);
        if (dateCol == null) {
            log.warn("Не найдена колонка с датами для листа: [{}]", sheetName);
            throw new IllegalArgumentException("Не найдена колонка с датами для листа: " + sheetName);
        }

        int startRow = 2; // config
        int insertRow = findFirstEmptyRow(sheetName, dateCol, startRow);

        if (operations == null || operations.isEmpty()) {
            List<Object> row = List.of(today);
            appendRow(row, buildCellRange(sheetName, dateCol, insertRow));
            log.info("Добавлена пустая строка с датой в таблицу: [{}]", sheetName);
            return;
        }

        for (FullReportDto.OperationDto dto : operations) {
            String type = dto.type();
            BigDecimal amount = dto.amount();
            String cashType = dto.cashType();
            String category = dto.category();
            String comment = dto.comment();

            Object income = "Приход".equalsIgnoreCase(type) //config
                    ? formatAmount(amount)
                    : null;
            Object expense = "Расход".equalsIgnoreCase(type) //config
                    ? formatAmount(amount)
                    : null;

            List<Object> row = buildOperationRow(today, income, expense, cashType, category, admin, comment);

            appendRow(row, buildCellRange(sheetName, dateCol, insertRow++));
            log.info("Сохранена операция: [{}]", type);
        }
        log.info("Загружено {} операций для пользователя [{} ({})]", operations.size(), admin, location);
    }

    private void updateTrialReport(FullReportDto.TrialReportDto dto, User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();
        String key = String.format("%s (%s)", admin, location);

        Map<String, String> entry = trialColumnsConfig.getColumns().get(key);
        if (entry == null || !entry.containsKey("sheet") || !entry.containsKey("range")) {
            log.warn("Неверный конфиг для пробного отчета пользователя: [{}]", key);
            throw new IllegalArgumentException("Неверный конфиг для пробного отчета пользователя : " + key);
        }

        String sheetName = entry.get("sheet");
        String[] range = entry.get("range").split(":");
        String startCol = range[0];
        String endCol = range[1];

        int row = findRowByDate(sheetName, "C", 8, today);

        List<Object> rowData = List.of(
                dto.trialCame(),
                dto.trialBought(),
                dto.trialBoughtAmount(),
                dto.trialPaid(),
                dto.trialPaidAmount(),
                dto.prepayment(),
                dto.prepaymentAmount(),
                dto.surcharge(),
                dto.surchargeAmount()
        );

        String targetRange = String.format("'%s'!%s%d:%s%d", sheetName, startCol, row, endCol, row);
        updateRow(rowData, targetRange);
        log.info("Загружен раздел \"Пробные\" для пользователя [{} ({})]", admin, location);
    }

    public void updateShiftReport(FullReportDto.ShiftReportDto dto, User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String dateCol = shiftColumnsConfig.getColumns().get(location);
        if (dateCol == null) {
            log.warn("Неизвестная локация: [{}]", location);
            throw new IllegalArgumentException("Неизвестная локация: " + location);
        }

        int row = findRowByDate(sheetNamesConfig.getShift(), dateCol, 3 , today);

        String startCol = nextColumn(dateCol);
        List<Object> rowData = List.of(
                dto.shiftStart(),
                dto.shiftEnd(),
                admin
        );

        String sheetName = sheetNamesConfig.getShift();
        String range = String.format("'%s'!%s%d:", sheetName, startCol, row);
        updateRow(rowData, range);
        log.info("Загружен раздел \"Касса в студии\" для пользователя [{} ({})]", admin, location);
    }

    public void updateCurrentReport(FullReportDto.CurrentReportDto dto, User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();
        String key = String.format("%s (%s)", admin, location);

        Map<String, String> entry = currentColumnsConfig.getColumns().get(key);
        if (entry == null || !entry.containsKey("sheet") || !entry.containsKey("range")) {
            log.warn("Неверный конфиг для текущего отчета пользователя: [{}]", key);
            throw new IllegalArgumentException("Неверный конфиг для текущего отчета пользователя: " + key);
        }

        String sheetName = entry.get("sheet");
        String[] range = entry.get("range").split(":");
        String startCol = range[0];
        String endCol = range[1];

        int row = findRowByDate(sheetName, "A", 6, today);

        List<Object> rowData = List.of(
                dto.finished(),
                dto.extended(),
                dto.extendedAmount(),
                dto.upgrades(),
                dto.upgradeAmount(),
                dto.returned(),
                dto.returnedAmount(),
                dto.prepayment(),
                dto.prepaymentAmount(),
                dto.surcharge(),
                dto.surchargeAmount(),
                dto.individual(),
                dto.individualAmount(),
                dto.singleVisits(),
                dto.singleVisitAmount()
        );

        String targetRange = String.format("'%s'!%s%d:%s%d", sheetName, startCol, row, endCol, row);
        updateRow(rowData, targetRange);
        log.info("Загружен раздел \"Текущие\" для пользователя [{} ({})]", admin, location);
    }

    private void rollbackTrialReport(User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();
        String key = String.format("%s (%s)", admin, location);

        Map<String, String> entry = trialColumnsConfig.getColumns().get(key);
        if(entry == null || !entry.containsKey("sheet") || !entry.containsKey("range")) {
            log.warn("Не удалось откатить изменения: Не найден конфиг для [{}]", key);
            throw new IllegalArgumentException("Конфиг пробных не найден для: " + key);
        }

        clearRowByDate(entry, "C", 8, today);
        log.info("Откат изменений: Очищен раздел \"Пробные\" для [{} ({})]", admin, location);
    }

    private void rollbackCurrentReport(User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();
        String key = String.format("%s (%s)", admin, location);

        Map<String, String> entry = currentColumnsConfig.getColumns().get(key);
        if(entry == null || !entry.containsKey("sheet") || !entry.containsKey("range")) {
            log.warn("Не удалось откатить изменения: Не найден конфиг для текущего отчета [{}]", key);
            throw new IllegalArgumentException("Конфиг текущих не найден для: " + key);
        }

        clearRowByDate(entry, "A", 6, today);
        log.info("Откат изменений: Очищен раздел \"Текущие\" для [{} ({})]", admin, location);
    }

    private void rollbackShiftReport(User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String dateCol = shiftColumnsConfig.getColumns().get(location);
        if (dateCol == null) {
            log.warn("Не удалось откатить \"Касса в студии\": Неизвестная локация: [{}]", location);
            throw new IllegalArgumentException("Неизвестная локация: " + location);
        }

        int row = findRowByDate(sheetNamesConfig.getShift(), dateCol, 3 , today);
        String startCol = nextColumn(dateCol);

        String range = String.format("'%s'!%s%d:%s%d",
                sheetNamesConfig.getShift(),
                startCol,
                row,
                nextColumn(nextColumn(startCol)),
                row
        );

        List<Object> emptyRow = List.of(EMPTY_CELL, EMPTY_CELL, EMPTY_CELL);
        updateRow(emptyRow, range);
        log.info("Откат изменений: Очищен раздел \"Касса в студии\" для [{} ({})]", admin, location);
    }

    private void updateRow(List<Object> row, String range) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption(VALUE_INPUT_OPTION)
                .execute();
    }

    private void appendRow(List<Object> row, String range) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption(VALUE_INPUT_OPTION)
                .execute();
    }

    private int findRowByDate(String sheetName, String column, int startRow, String date) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheetName, column, startRow, column);
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> rows = response.getValues();
        if (rows == null) return -1;

        for (int relativeRowOffset = 0; relativeRowOffset < rows.size(); relativeRowOffset++) {
            if (!rows.get(relativeRowOffset).isEmpty() && date.equals(rows.get(relativeRowOffset).getFirst().toString())) {
                return startRow + relativeRowOffset;
            }
        }
        throw new IllegalArgumentException("Дата не найдена: " + date);
    }

    private int findFirstEmptyRow(String sheetName, String column, int startRow) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheetName, column, startRow, column);
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> rows = response.getValues();
        if (rows == null) return startRow;

        return startRow + rows.size();
    }

    private String nextColumn(String column){
        int length = column.length();
        char[] chars = column.toUpperCase().toCharArray();

        for (int i = length - 1; i >= 0; i--) {
            if (chars[i] != 'Z') {
                chars[i]++;
                return new String (chars, 0, i + 1);
            } else {
                chars[i] = 'A';
            }
        }

        return "A" + new String(chars);
    }

    private int columnCount(String startCol, String endCol){
        return columnToIndex(endCol) - columnToIndex(startCol) + 1;
    }

    private int columnToIndex(String col) {
        int result = 0;
        for (char c : col.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result;
    }

    private Object formatAmount(BigDecimal amount) {
        if(amount == null) return null;
        BigDecimal clean = amount.stripTrailingZeros();
        return clean.scale() <= 0 ? clean.longValue() : clean.doubleValue();
    }

    private List<Object> buildOperationRow(String today,
                                           Object income,
                                           Object expense,
                                           String cashType,
                                           String category,
                                           String admin,
                                           String comment
                                           ) {
        List<Object> row = new ArrayList<>(7);
        row.add(today);
        row.add(income);
        row.add(expense);
        row.add(cashType);
        row.add(category);
        row.add(admin);
        row.add(comment);
        return row;
    }

    private String buildCellRange(String sheetName, String column, int row) {
        return String.format("'%s'!%s%d", sheetName, column, row);
    }

    private void clearRowByDate(Map<String, String> entry, String column, int startRow, String today) throws IOException {
        String sheetName = entry.get("sheet");
        String[] range = entry.get("range").split(":");
        String startCol = range[0];
        String endCol = range[1];

        int row = findRowByDate(sheetName, column, startRow, today);

        String targetRange = String.format("'%s'!%s%d:%s%d", sheetName, startCol, row, endCol, row);

        int columnCount = columnCount(startCol, endCol);
        List<Object> emptyRow = new ArrayList<>(Collections.nCopies(columnCount, EMPTY_CELL));

        updateRow(emptyRow, targetRange);
    }
}
