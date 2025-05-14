package com.levita.levita_monitoring.service.parser;

public interface LocationKpiUpdater {
    void update(
            String topic,
            String rawLocation,
            String rawValue
    );
}
