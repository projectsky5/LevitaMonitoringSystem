package com.levita.levita_monitoring.service.parser;

public interface UserKpiUpdater {
    void update(
            String topic,
            String rawUser,
            String rawValue
    );
}
