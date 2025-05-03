package com.levita.levita_monitoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "trial-columns")
public class TrialColumnsConfig {

    private Map<String, String> columns;

    public Map<String, String> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, String> columns) {
        this.columns = columns;
    }
}
