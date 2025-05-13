package com.levita.levita_monitoring.service;

import java.io.IOException;
import java.util.List;

public interface SheetsClient {
    List<List<Object>> getValues(String range) throws IOException;
    void appendRow(String range, List<Object> row) throws IOException;
    void updateValues(String range, List<List<Object>> values) throws IOException;
    int getSheetIdByName(String sheetName) throws IOException;
    void deleteRows(String sheetName, List<Integer> rows) throws IOException;
}
