package com.levita.levita_monitoring.integration;

import com.levita.levita_monitoring.service.AsyncParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SheetsParserTest {

    private AsyncParserService asyncParserService;
    private SheetsParser sheetsParser;

    @BeforeEach
    void setUp() {
        asyncParserService = Mockito.mock(AsyncParserService.class);
        sheetsParser = new SheetsParser(asyncParserService);
    }

    @Test
    void scheduleDataParsing_triggersAsyncParserService() {
        sheetsParser.scheduleDataParsing();

        Mockito.verify(asyncParserService, Mockito.times(1)).runParser();
    }
}