package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.CurrentColumnsConfig;
import com.levita.levita_monitoring.configuration.DateColumnsConfig;
import com.levita.levita_monitoring.configuration.SheetNamesConfig;
import com.levita.levita_monitoring.configuration.ShiftColumnsConfig;
import com.levita.levita_monitoring.dto.reportDto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM - E", Locale.forLanguageTag("ru-RU"));

    public SheetsReportService(Sheets sheets, ShiftColumnsConfig shiftColumnsConfig, SheetNamesConfig sheetNamesConfig, CurrentColumnsConfig currentColumnsConfig, DateColumnsConfig dateColumnsConfig) {
        this.sheets = sheets;
        this.shiftColumnsConfig = shiftColumnsConfig;
        this.sheetNamesConfig = sheetNamesConfig;
        this.currentColumnsConfig = currentColumnsConfig;
        this.dateColumnsConfig = dateColumnsConfig;
    }

    public void insertDateRowIfMissing() throws IOException {
        String today = LocalDate.now().format(dateFormatter);

        for(Map.Entry<String, String> entry : dateColumnsConfig.getColumns().entrySet()){
            String sheetName = entry.getKey();
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
                    String insertRange = String.format("'%s'!%s%d", sheetName, col, rowToInsert);

                    ValueRange body = new ValueRange().setValues(List.of(List.of(today)));

                    sheets.spreadsheets().values()
                            .update(spreadsheetId, insertRange, body)
                            .setValueInputOption("USER-ENTERED")
                            .execute();
                }
            }
        }

    }

    public void appendFullReport(FullReportDto dto, User user) throws IOException {
        String today = LocalDate.now().format(dateFormatter);

        updateShiftReport(dto.shift(), user, today);
//        appendTrialReport(dto.trial(), user, today);
        updateCurrentReport(dto.current(), user, today);
//        appendOperationsReport(dto.operations(), user, today);
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

        String sheetName = switch(location){
            case "Варварская", "Чугунова", "Родионова", "Мещерский", "Богородского" -> sheetNamesConfig.getCurrent() + " (команда Кати)";
            default -> sheetNamesConfig.getCurrent() + " (команда Алины)";
        };

        // Ключ для поиска в конфиге
        String key = String.format("%s (%s)", admin, location);
        String rangeDef = currentColumnsConfig.getColumns().get(key);

        if(rangeDef == null || !rangeDef.contains(":")) {
            throw new IllegalArgumentException("Неверный диапазон колонок для пользователя: " + key);
        }

        String[] colRange = rangeDef.split(":");
        String startCol = colRange[0];
        String endCol = colRange[1];

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

        String range = String.format("'%s'!%s%d:%s%d", sheetName, startCol, row, endCol, row);
        updateRow(rowData, range);
    }

    private void updateRow(List<Object> row, String range) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
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
}
