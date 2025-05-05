package com.levita.levita_monitoring.configuration.sheet_reports;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "trial-columns")
public class TrialColumnsConfig {

    private Map<String, Map<String, String>> columns;

    public Map<String, Map<String, String>> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Map<String, String>> columns) {
        this.columns = columns;
    }
}
