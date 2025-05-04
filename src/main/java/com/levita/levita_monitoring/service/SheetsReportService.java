package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.*;
import com.levita.levita_monitoring.dto.reportDto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SheetsReportService {

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

        for(Map.Entry<String, String> entry : dateColumnsConfig.getColumns().entrySet()){
            String sheetName = entry.getKey();
            if (sheetName.startsWith("Журнал операций")){
                continue;
            }

            String col = entry.getValue();

            int startRow = switch (sheetName){
                case "Пробные (команда Кати)", "Пробные (команда Алины)" -> 8;
                case "Текущие (команда Кати)", "Текущие (команда Алины)" -> 6;
                default -> 3;
            };

            String range = String.format("'%s'!%s%d:%s", sheetName, col, startRow, col);

            ValueRange response = sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values != null) {
                boolean alreadyExists = values.stream()
                        .anyMatch(row -> !row.isEmpty() && today.equals(row.get(0).toString()));

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
        }
    }

    public void updateFullReport(FullReportDto dto, User user) throws IOException {
        String today = LocalDate.now().format(dateFormatter);

        updateShiftReport(dto.shift(), user, today);
        updateTrialReport(dto.trial(), user, today);
        updateCurrentReport(dto.current(), user, today);
        saveOperationsForDate(dto.operations(), user, today);
    }

    private void saveOperationsForDate(List<FullReportDto.OperationDto> operations, User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String sheetName = sheetNamesConfig.getOperations().get(location);
        if (sheetName == null) {
            throw new IllegalArgumentException("Не найден лист для операций по локации: " + location);
        }

        String dateCol = dateColumnsConfig.getColumns().get(sheetName);
        if (dateCol == null) {
            throw new IllegalArgumentException("Не найдена колонка с датами для листа: " + sheetName);
        }

        int startRow = 2;
        int insertRow = findFirstEmptyRow(sheetName, dateCol, startRow);

        if (operations == null || operations.isEmpty()) {
            List<Object> row = List.of(today);
            appendRow(row, buildCellRange(sheetName, dateCol, insertRow));
            return;
        }

        for (FullReportDto.OperationDto dto : operations) {
            String type = dto.type();
            BigDecimal amount = dto.amount();
            String cashType = dto.cashType();
            String category = dto.category();
            String comment = dto.comment();

            Object income = "Приход".equalsIgnoreCase(type)
                    ? formatAmount(amount)
                    : null;
            Object expense = "Расход".equalsIgnoreCase(type)
                    ? formatAmount(amount)
                    : null;

            List<Object> row = buildOperationRow(today, income, expense, cashType, category, admin, comment);

            appendRow(row, buildCellRange(sheetName, dateCol, insertRow++));
        }
    }

    private void updateTrialReport(FullReportDto.TrialReportDto dto, User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();
        String key = String.format("%s (%s)", admin, location);

        Map<String, String> entry = trialColumnsConfig.getColumns().get(key);
        if (entry == null || !entry.containsKey("sheet") || !entry.containsKey("range")) {
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
    }

    public void updateShiftReport(FullReportDto.ShiftReportDto dto, User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String dateCol = shiftColumnsConfig.getColumns().get(location);
        if (dateCol == null) {
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
    }

    public void updateCurrentReport(FullReportDto.CurrentReportDto dto, User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();
        String key = String.format("%s (%s)", admin, location);

        Map<String, String> entry = currentColumnsConfig.getColumns().get(key);
        if (entry == null || !entry.containsKey("sheet") || !entry.containsKey("range")) {
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
    }

    private void updateRow(List<Object> row, String range) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER-ENTERED")
                .execute();
    }

    private void appendRow(List<Object> row, String range) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER-ENTERED")
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
            if (!rows.get(relativeRowOffset).isEmpty() && date.equals(rows.get(relativeRowOffset).get(0).toString())) {
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
}
