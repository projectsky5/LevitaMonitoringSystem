package com.levita.levita_monitoring.configuration.sheet_reports;

import java.util.Map;

public class ShiftColumnsDescriptor {

    private Map<String, String> shiftColumns;

    public ShiftColumnsDescriptor() {
    }

    public Map<String, String> getShiftColumns() {
        return shiftColumns;
    }

    public void setShiftColumns(Map<String, String> shiftColumns) {
        this.shiftColumns = shiftColumns;
    }
}
