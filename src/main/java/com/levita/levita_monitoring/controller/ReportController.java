package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.service.SheetsReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final SheetsReportService sheetsReportService;

    public ReportController(SheetsReportService sheetsReportService) {
        this.sheetsReportService = sheetsReportService;
    }

    @PostMapping
    public ResponseEntity<?> submitReport(@Valid @RequestBody FullReportDto dto,
                                          @AuthenticationPrincipal User user) throws IOException {
        log.info("Отчет получен от пользователя [{}] в локации [{}] в {}",
                user.getName(),
                user.getLocation().getName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        sheetsReportService.updateFullReport(dto, user);
        return ResponseEntity.ok("Отчет успешно загружен");
    }
}
