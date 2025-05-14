package com.levita.levita_monitoring.service.parser;

public interface LocationCreator {
    boolean createIfNotExists(
            String rawLocation
    );
}
