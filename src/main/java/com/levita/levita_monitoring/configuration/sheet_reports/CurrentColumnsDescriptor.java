package com.levita.levita_monitoring.configuration.sheet_reports;

import java.util.Map;

public class CurrentColumnsDescriptor {

    private Map<String, Entry> currentColumns;

    public CurrentColumnsDescriptor() {
    }

    public Map<String, Entry> getCurrentColumns() {
        return currentColumns;
    }

    public void setCurrentColumns(Map<String, Entry> currentColumns) {
        this.currentColumns = currentColumns;
    }

    public static class Entry {
        private String sheet;
        private String range;

        public Entry() {
        }

        public String getSheet() {
            return sheet;
        }

        public void setSheet(String sheet) {
            this.sheet = sheet;
        }

        public String getRange() {
            return range;
        }

        public void setRange(String range) {
            this.range = range;
        }
    }
}
