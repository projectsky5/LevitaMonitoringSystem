package com.levita.levita_monitoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "current-columns")
public class CurrentColumnsConfig {

    private Map<String, List<String>> columns;

    public Map<String, List<String>> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, List<String>> columns) {
        this.columns = columns;
    }
}
