package com.levita.levita_monitoring.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncParserService {

    private final SheetsParserService sheetsParserService;

    public AsyncParserService(SheetsParserService sheetsParserService) {
        this.sheetsParserService = sheetsParserService;
    }

    @Async
    public void runParser() {
        sheetsParserService.runSheetsParser();
    }
}
