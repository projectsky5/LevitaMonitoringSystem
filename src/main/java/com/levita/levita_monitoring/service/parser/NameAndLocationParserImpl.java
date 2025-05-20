package com.levita.levita_monitoring.service.parser;

import org.springframework.stereotype.Service;

@Service
public class NameAndLocationParserImpl implements NameAndLocationParser {
    @Override
    public String[] parse(String raw) {
        String cleaned = raw == null ? "" : raw.trim();
        String[] parts = cleaned.split("\\s*\\(|\\)");
        if (parts.length == 2) {
            return new String[]{ parts[0].trim(), parts[1].trim() };
        }
        return new String[]{ cleaned, "" };
    }
}
