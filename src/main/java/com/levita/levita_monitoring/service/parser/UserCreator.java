package com.levita.levita_monitoring.service.parser;

public interface UserCreator {
    boolean createIfNotExists(
            String rawUser
    );
}
