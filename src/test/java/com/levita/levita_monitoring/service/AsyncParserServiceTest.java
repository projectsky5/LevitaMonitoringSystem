package com.levita.levita_monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncParserServiceTest {

    @Mock
    private SheetsParserService sheetsParserService;

    @InjectMocks
    private AsyncParserService asyncParserService;

    @Test
    void runParser_delegatesToSheetsParserService() {
        asyncParserService.runParser();

        verify(sheetsParserService, times(1)).runSheetsParser();
    }

    @Test
    void runParser_isAnnotatedWithAsync() throws NoSuchMethodException {
        Method m = AsyncParserService.class.getMethod("runParser");
        assertTrue(m.isAnnotationPresent(Async.class),
                "runParser should be annotated with @Async");
    }
}