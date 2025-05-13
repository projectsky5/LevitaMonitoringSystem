package com.levita.levita_monitoring.service.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.levita.levita_monitoring.service.SheetsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GoogleSheetsClientImpl implements SheetsClient {

    private final Sheets sheets;
    private final String spreadsheetId;

    public GoogleSheetsClientImpl(Sheets sheets,
                                  @Value("${google.sheets.spreadsheetId}") String spreadsheetId) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
    }

    @Override
    public List<List<Object>> getValues(String range) throws IOException {
        ValueRange response = sheets
                .spreadsheets()
                .values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    @Override
    public void appendRow(String range, List<Object> row) throws IOException {
        sheets.spreadsheets()
                .values()
                .append(spreadsheetId, range, new ValueRange().setValues(List.of(row)))
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    @Override
    public void updateValues(String range, List<List<Object>> values) throws IOException {
        // Формируем пакетное обновление: один ValueRange с нужным диапазоном и набором значений
        BatchUpdateValuesRequest batchRequest = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(List.of(
                        new ValueRange()
                                .setRange(range)
                                .setValues(values)
                ));

        // Отправляем batchUpdate для одного из нескольких диапазонов за один HTTP вызов
        sheets.spreadsheets()
                .values()
                .batchUpdate(spreadsheetId, batchRequest)
                .execute();
    }

    @Override
    public int getSheetIdByName(String sheetName) throws IOException {
        // один HTTP-запрос, возвращаем нужный sheetId
        return sheets.spreadsheets()
                .get(spreadsheetId)
                .execute()
                .getSheets().stream()
                .filter(s -> sheetName.equals(s.getProperties().getTitle()))
                .findFirst()
                .map(s -> s.getProperties().getSheetId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Идентификатор листа не найден: " + sheetName)
                );
    }

    @Override
    public void deleteRows(String sheetName, List<Integer> rows) throws IOException {
        int sheetId = getSheetIdByName(sheetName);
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

}
