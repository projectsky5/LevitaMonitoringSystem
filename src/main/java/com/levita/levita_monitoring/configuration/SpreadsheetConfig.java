package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.SheetsRanges;

import java.util.List;
import java.util.Objects;


public class SpreadsheetConfig {
    private String spreadsheetId; // ID таблицы
    private String credentialsFile; // Путь до файла credentials
    private List<SheetsRanges> ranges; // Список диапазонов для этой таблицы

    public SpreadsheetConfig(String spreadsheetId, String credentialsFile, List<SheetsRanges> ranges) {
        this.spreadsheetId = spreadsheetId;
        this.credentialsFile = credentialsFile;
        this.ranges = ranges;
    }

    public SpreadsheetConfig() {
    }

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public String getCredentialsFile() {
        return credentialsFile;
    }

    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public List<SheetsRanges> getRanges() {
        return ranges;
    }

    public void setRanges(List<SheetsRanges> ranges) {
        this.ranges = ranges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpreadsheetConfig that = (SpreadsheetConfig) o;
        return Objects.equals(spreadsheetId, that.spreadsheetId) && Objects.equals(credentialsFile, that.credentialsFile) && Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spreadsheetId, credentialsFile, ranges);
    }

    @Override
    public String toString() {
        return "SpreadsheetConfig{" +
                "spreadsheetId='" + spreadsheetId + '\'' +
                ", credentialsFile='" + credentialsFile + '\'' +
                ", ranges=" + ranges +
                '}';
    }
}
