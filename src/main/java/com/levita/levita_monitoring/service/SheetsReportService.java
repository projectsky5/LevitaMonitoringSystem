package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.CurrentColumnsConfig;
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

@Service
public class SheetsReportService {

    private final Sheets sheets;
    private final ShiftColumnsConfig shiftColumnsConfig;
    private final SheetNamesConfig sheetNamesConfig;
    private final CurrentColumnsConfig currentColumnsConfig;

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM - E", Locale.forLanguageTag("ru-RU"));

    public SheetsReportService(Sheets sheets, ShiftColumnsConfig shiftColumnsConfig, SheetNamesConfig sheetNamesConfig, CurrentColumnsConfig currentColumnsConfig) {
        this.sheets = sheets;
        this.shiftColumnsConfig = shiftColumnsConfig;
        this.sheetNamesConfig = sheetNamesConfig;
        this.currentColumnsConfig = currentColumnsConfig;
    }

    public void appendFullReport(FullReportDto dto, User user) throws IOException {
        String today = LocalDate.now().format(dateFormatter);

        appendShiftReport(dto.shift(), user, today);
//        appendTrialReport(dto.trial(), user, today);
        appendCurrentReport(dto.current(), user, today);
//        appendOperationsReport(dto.operations(), user, today);
    }

    public void appendShiftReport(FullReportDto.ShiftReportDto dto, User user, String today) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String startCol = shiftColumnsConfig.getColumns().get(location);
        if (startCol == null) {
            throw new IllegalArgumentException("Неизвестная локация: " + location);
        }

        List<Object> row = List.of(
                today,
                dto.shiftStart(),
                dto.shiftEnd(),
                admin
        );

        String sheetName = sheetNamesConfig.getShift();
        String range = sheetName + "!" + startCol + ":";
        appendRow(row, range);
    }

    public void appendCurrentReport(FullReportDto.CurrentReportDto dto, User user, String today) throws IOException {
        String admin = user.getName();
        String location = user.getLocation().getName();

        String sheetName = switch(location){
            case "Варварская", "Чугунова", "Родионова", "Мещерский", "Богородского" -> sheetNamesConfig.getCurrent() + " (команда Кати)";
            default -> sheetNamesConfig.getCurrent() + " (команда Алины)";
        };

        // Ключ для поиска в конфиге
        String key = admin + " (" + location + ")";
        List<String> columns = currentColumnsConfig.getColumns().get(key);

        if(columns == null){
            throw new IllegalArgumentException("Не найдены координаты ячеек для пользователя: " + location);
        }

        List<Object> row = List.of(
                today,
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

        String range = sheetName + "!" + columns.getFirst() + ":";
        appendRow(row, range);
    }

    private void appendRow(List<Object> row, String range) throws IOException {
        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER-ENTERED")
                .execute();
    }
}
