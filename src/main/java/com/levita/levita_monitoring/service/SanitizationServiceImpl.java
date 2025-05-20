package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.util.SanitizationUtils;
import org.springframework.stereotype.Service;

@Service
public class SanitizationServiceImpl implements SanitizationService {
    @Override
    public String sanitize(String raw) {
        return SanitizationUtils.sanitizeNumeric(raw);
    }
}
