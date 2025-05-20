package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.util.SanitizationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SanitizationServiceImplTest {

    private final SanitizationServiceImpl service = new SanitizationServiceImpl();

    @Test
    void sanitize_delegatesToSanitizationUtils() {
        String raw = "  123abc.45 ";
        String cleaned = "123.45";

        try (MockedStatic<SanitizationUtils> mocks = mockStatic(SanitizationUtils.class)) {
            mocks.when(() -> SanitizationUtils.sanitizeNumeric(raw)).thenReturn(cleaned);

            String result = service.sanitize(raw);

            assertSame(cleaned, result, "Should return exactly what SanitizationUtils.sanitizeNumeric returns");
            mocks.verify(() -> SanitizationUtils.sanitizeNumeric(raw), times(1));
        }
    }

    @Test
    void sanitize_handlesNullInput() {
        try (MockedStatic<SanitizationUtils> mocks = mockStatic(SanitizationUtils.class)) {
            mocks.when(() -> SanitizationUtils.sanitizeNumeric(null)).thenReturn("");

            String result = service.sanitize(null);

            assertEquals("", result, "Null raw input should be forwarded to sanitizeNumeric and return its result");
            mocks.verify(() -> SanitizationUtils.sanitizeNumeric(null), times(1));
        }
    }
}