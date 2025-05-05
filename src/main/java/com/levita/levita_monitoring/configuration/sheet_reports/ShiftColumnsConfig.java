package com.levita.levita_monitoring.configuration.sheet_reports;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "shift-columns")
public class ShiftColumnsConfig {

    private Map<String, String> columns;

    public Map<String, String> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, String> columns) {
        this.columns = columns;
    }
}
