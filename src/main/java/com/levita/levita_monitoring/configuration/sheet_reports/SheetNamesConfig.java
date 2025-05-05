package com.levita.levita_monitoring.configuration.sheet_reports;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "sheet-names")
public class SheetNamesConfig {

    private String shift;
    private String trial;
    private String current;
    private Map<String, String> operations;

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getTrial() {
        return trial;
    }

    public void setTrial(String trial) {
        this.trial = trial;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public Map<String, String> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, String> operations) {
        this.operations = operations;
    }
}
