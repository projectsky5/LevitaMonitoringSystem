// TODO: 08 04 2025 - Переделать под один файл credentials (сделать доступ у разных таблиц к 1 гугл проекту)

package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.enums.SheetsRanges;

import java.util.List;

public class SpreadsheetConfig {
    private String spreadsheetId; // ID таблицы
    private List<SheetsRanges> ranges; // Список диапазонов для этой таблицы

    public SpreadsheetConfig(String spreadsheetId, List<SheetsRanges> ranges) {
        this.spreadsheetId = spreadsheetId;
        this.ranges = ranges;
    }

    public SpreadsheetConfig() {
    }


    public String getSpreadsheetId() {
        return this.spreadsheetId;
    }

    public List<SheetsRanges> getRanges() {
        return this.ranges;
    }

    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public void setRanges(List<SheetsRanges> ranges) {
        this.ranges = ranges;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SpreadsheetConfig)) return false;
        final SpreadsheetConfig other = (SpreadsheetConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$spreadsheetId = this.getSpreadsheetId();
        final Object other$spreadsheetId = other.getSpreadsheetId();
        if (this$spreadsheetId == null ? other$spreadsheetId != null : !this$spreadsheetId.equals(other$spreadsheetId))
            return false;
        final Object this$ranges = this.getRanges();
        final Object other$ranges = other.getRanges();
        if (this$ranges == null ? other$ranges != null : !this$ranges.equals(other$ranges)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SpreadsheetConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $spreadsheetId = this.getSpreadsheetId();
        result = result * PRIME + ($spreadsheetId == null ? 43 : $spreadsheetId.hashCode());
        final Object $ranges = this.getRanges();
        result = result * PRIME + ($ranges == null ? 43 : $ranges.hashCode());
        return result;
    }

    public String toString() {
        return "SpreadsheetConfig(spreadsheetId=" + this.getSpreadsheetId() + ", ranges=" + this.getRanges() + ")";
    }
}
