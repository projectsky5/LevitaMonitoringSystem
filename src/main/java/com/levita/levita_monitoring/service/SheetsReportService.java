package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
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

    private static final String VALUE_INPUT_OPTION = "USER_ENTERED";
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

        List<ValueRange> batchRanges = new ArrayList<>();
        batchRanges.add(buildShiftValueRange(dto.shift(), user, today));
        batchRanges.add(buildTrialValueRange(dto.trial(), user, today));
        batchRanges.add(buildCurrentValueRange(dto.current(), user, today));

        if (!batchRanges.isEmpty()) {
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption(VALUE_INPUT_OPTION)
                    .setData(batchRanges);
            sheets.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody)
                    .execute();
        }

        saveOperationsForDate(dto.operations(), user, today);

        Instant end = Instant.now();
        log.info("Загрузка данных за дату {} завершена за {} мс", today,
                Duration.between(start, end).toMillis());
    }

    public void rollbackFullReport(User user) throws IOException {
        String today = LocalDate.now().format(dateFormatter);
        log.info("{} - Начало полного отката данных", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Instant start = Instant.now();

        List<ValueRange> clearRanges = new ArrayList<>();
        clearRanges.add(buildClearShiftValueRange(user, today));
        clearRanges.add(buildClearTrialValueRange(user, today));
        clearRanges.add(buildClearCurrentValueRange(user, today));

        if(!clearRanges.isEmpty()) {
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption(VALUE_INPUT_OPTION)
                    .setData(clearRanges);
            sheets.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody)
                    .execute();
        }

        rollbackOperations(user, today);

        Instant end = Instant.now();
        log.info("Полный откат данных за дату {} завершен за {} мс", today,
                Duration.between(start, end).toMillis());
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

    private ValueRange buildTrialValueRange(FullReportDto.TrialReportDto dto, User user, String today) throws IOException {
        String key = String.format("%s (%s)", user.getName(), user.getLocation().getName());
        Map<String, String> config = trialColumnsConfig.getColumns().get(key);
        String sheet = config.get("sheet");
        String[] cols = config.get("range").split(":");
        int row = findRowByDate(sheet, "C", 8, today);
        List<Object> data = List.of(
                dto.trialCame(), dto.trialBought(), dto.trialBoughtAmount(),
                dto.trialPaid(), dto.trialPaidAmount(), dto.prepayment(),
                dto.prepaymentAmount(), dto.surcharge(), dto.surchargeAmount()
        );
        String range = String.format("'%s'!%s%d:%s%d", sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    private ValueRange buildShiftValueRange(FullReportDto.ShiftReportDto dto, User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String dateCol = shiftColumnsConfig.getColumns().get(location);
        int row = findRowByDate(sheetNamesConfig.getShift(), dateCol, 3 , today);
        String startCol = nextColumn(dateCol);
        String endCol = nextColumn(startCol);
        List<Object> data = List.of(
                dto.shiftStart(),
                dto.shiftEnd(),
                user.getName()
        );
        String range = String.format("'%s'!%s%d:%s%d", sheetNamesConfig.getShift(), startCol, row, endCol, row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    private ValueRange buildCurrentValueRange(FullReportDto.CurrentReportDto dto, User user, String today) throws IOException {
        String key = String.format("%s (%s)", user.getName(), user.getLocation().getName());
        Map<String, String> config = currentColumnsConfig.getColumns().get(key);
        String sheet = config.get("sheet");
        String[] cols = config.get("range").split(":");
        int row = findRowByDate(sheet, "A", 6, today);
        List<Object> data = List.of(
                dto.finished(), dto.extended(), dto.extendedAmount(),
                dto.upgrades(), dto.upgradeAmount(), dto.returned(), dto.returnedAmount(),
                dto.prepayment(), dto.prepaymentAmount(), dto.surcharge(), dto.surchargeAmount(),
                dto.individual(), dto.individualAmount(), dto.singleVisits(), dto.singleVisitAmount()
        );
        String range = String.format("'%s'!%s%d:%s%d", sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(data));
    }

    private void rollbackOperations(User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String sheetName = sheetNamesConfig.getOperations().get(location);
        if (sheetName == null) {
            log.warn("Откат операций невозможен: не найден лист для локации [{}]", location);
            throw new IllegalArgumentException("Не найден лист операций для локации: " + location);
        }

        String dateCol = dateColumnsConfig.getColumns().get(sheetName);
        if (dateCol == null) {
            log.warn("Откат операций невозможен: не найдена колонка с датами для [{}]", sheetName);
            throw new IllegalArgumentException("Не найдена колонка с датами для листа: " + sheetName);
        }

        int startRow = 2;
        List<Integer> rowsToDelete = findRowsByDate(sheetName, dateCol, startRow, today);

        if(rowsToDelete.isEmpty()) {
            log.info("Откат операций: строк за дату [{}] не найдено", today);
            return;
        }

        deleteRows(sheetName, rowsToDelete);
        log.info("Откат операций: удалено {} строк за дату [{}] для [{} ({})]",
                rowsToDelete.size(), today, admin, location);
    }

    private ValueRange buildClearTrialValueRange(User user, String today) throws IOException {
        String key = String.format("%s (%s)", user.getName(), user.getLocation().getName());
        Map<String,String> config = trialColumnsConfig.getColumns().get(key);
        String sheet = config.get("sheet");
        String[] cols = config.get("range").split(":");
        int row = findRowByDate(sheet, "C", 8, today);
        int count = columnToIndex(cols[1]) - columnToIndex(cols[0]) + 1;
        List<Object> empty = Collections.nCopies(count, EMPTY_CELL);
        String range = String.format("'%s'!%s%d:%s%d", sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(empty));
    }

    private ValueRange buildClearShiftValueRange(User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String dateCol = shiftColumnsConfig.getColumns().get(location);
        int row = findRowByDate(sheetNamesConfig.getShift(), dateCol, 3, today);
        String startCol = nextColumn(dateCol);
        String endCol = nextColumn(startCol);
        int count = columnToIndex(endCol) - columnToIndex(startCol) + 1;
        List<Object> empty = Collections.nCopies(count, EMPTY_CELL);
        String range = String.format("'%s'!%s%d:%s%d", sheetNamesConfig.getShift(), startCol, row, endCol, row);
        return new ValueRange().setRange(range).setValues(List.of(empty));
    }

    private ValueRange buildClearCurrentValueRange(User user, String today) throws IOException {
        String key = String.format("%s (%s)", user.getName(), user.getLocation().getName());
        Map<String,String> config = currentColumnsConfig.getColumns().get(key);
        String sheet = config.get("sheet");
        String[] cols = config.get("range").split(":");
        int row = findRowByDate(sheet, "A", 6, today);
        int count = columnToIndex(cols[1]) - columnToIndex(cols[0]) + 1;
        List<Object> empty = Collections.nCopies(count, EMPTY_CELL);
        String range = String.format("'%s'!%s%d:%s%d", sheet, cols[0], row, cols[1], row);
        return new ValueRange().setRange(range).setValues(List.of(empty));
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

    private List<Integer> findRowsByDate(String sheetName, String column, int startRow, String date) throws IOException {
        String range = String.format("'%s'!%s%d:%s", sheetName, column, startRow, column);
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> rows = response.getValues();
        List<Integer> matchedRows = new ArrayList<>();

        if (rows == null) return matchedRows;

        for (int i = 0; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            if(!row.isEmpty() && date.equals(row.getFirst().toString())) {
                matchedRows.add(startRow + i);
            }
        }
        return matchedRows;
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

    private void clearRange(String sheetName, String startCol, String endCol, int row) throws IOException {
        String range = String.format("'%s'!%s%d:%s%d", sheetName, startCol, row, endCol, row);
        int columnCount = columnCount(startCol, endCol);
        List<Object> emptyRow = emptyRow(columnCount);
        updateRow(emptyRow, range);
    }

    private void deleteRows(String sheetName, List<Integer> rowsToDelete) throws IOException {
        Integer sheetId = getSheetIdByName(sheetName);

        rowsToDelete.sort(Comparator.reverseOrder());

        List<Request> requests = new ArrayList<>();
        for (Integer row : rowsToDelete) {
            requests.add(new Request().setDeleteDimension(new DeleteDimensionRequest()
                    .setRange(new DimensionRange()
                            .setSheetId(sheetId)
                            .setDimension("ROWS")
                            .setStartIndex(row - 1)
                            .setEndIndex(row)
                    )
            ));
        }

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }

//    Метод получения идентификатора листа для DeleteDimensionRequest
    private Integer getSheetIdByName(String sheetName) throws IOException {
        Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();
        return spreadsheet.getSheets().stream()
                .filter(s -> sheetName.equals(s.getProperties().getTitle()))
                .findFirst()
                .map(s -> s.getProperties().getSheetId())
                .orElseThrow(() -> new IllegalArgumentException("Идентификатор листа не найден: " + sheetName));
    }

    private List<Object> emptyRow(int size){
        return new ArrayList<>(Collections.nCopies(size, EMPTY_CELL));
    }
}
