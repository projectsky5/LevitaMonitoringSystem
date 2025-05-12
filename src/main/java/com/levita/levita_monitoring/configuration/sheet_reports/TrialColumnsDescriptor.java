package com.levita.levita_monitoring.configuration.sheet_reports;

import java.util.Map;

public class TrialColumnsDescriptor {

    private Map<String, Entry> trialColumns;

    public TrialColumnsDescriptor() {
    }

    public Map<String, Entry> getTrialColumns() {
        return trialColumns;
    }

    public void setTrialColumns(Map<String, Entry> trialColumns) {
        this.trialColumns = trialColumns;
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
