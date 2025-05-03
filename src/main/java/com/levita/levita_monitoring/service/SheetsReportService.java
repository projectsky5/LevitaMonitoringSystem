package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
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

    @Value("${google.sheets.spreadsheetId}")
    private String spreadsheetId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM - E", Locale.forLanguageTag("ru-RU"));

    public SheetsReportService(Sheets sheets, ShiftColumnsConfig shiftColumnsConfig, SheetNamesConfig sheetNamesConfig) {
        this.sheets = sheets;
        this.shiftColumnsConfig = shiftColumnsConfig;
        this.sheetNamesConfig = sheetNamesConfig;
    }

    public void appendShiftReport(FullReportDto.ShiftReportDto dto, User user) throws IOException {
        String location = user.getLocation().getName();
        String admin = user.getName();

        String startCol = shiftColumnsConfig.getColumns().get(location);
        if (startCol == null) {
            throw new IllegalArgumentException("Неизвестная локация: " + location);
        }

        String date = LocalDate.now().format(dateFormatter);

        List<Object> row = List.of(
                date,
                dto.shiftStart(),
                dto.shiftEnd(),
                admin
        );

        String sheetName = sheetNamesConfig.getShift();
        String range = sheetName + "!" + startCol + ":";

        ValueRange body = new ValueRange().setValues(List.of(row));

        sheets.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER-ENTERED")
                .execute();
    }





}
