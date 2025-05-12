package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.dto.RollbackReportDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.dto.ReportStatusDto;
import com.levita.levita_monitoring.security.CustomUserDetails;
import com.levita.levita_monitoring.service.ReportSubmissionService;
import com.levita.levita_monitoring.service.SheetsReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final SheetsReportService sheetsReportService;
    private final ReportSubmissionService reportSubmissionService;

    public ReportController(SheetsReportService sheetsReportService, ReportSubmissionService reportSubmissionService) {
        this.sheetsReportService = sheetsReportService;
        this.reportSubmissionService = reportSubmissionService;
    }

    @PostMapping
    public ResponseEntity<?> submitReport(@Valid @RequestBody FullReportDto dto,
                                          @AuthenticationPrincipal CustomUserDetails principal) throws IOException {
        User user = principal.getUser();
        log.info("Отчет получен от пользователя [{}] в локации [{}] в {}",
                user.getName(),
                user.getLocation().getName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        sheetsReportService.updateFullReport(user, dto);
        reportSubmissionService.submitReport(user.getId());
        return ResponseEntity.ok(Map.of("message", "Отчет успешно загружен"));
    }

    @PostMapping("/rollback")
    public ResponseEntity<?> rollbackReport(@AuthenticationPrincipal CustomUserDetails principal,
                                            @Valid @RequestBody RollbackReportDto dto) throws IOException {
        User user = principal.getUser();
        log.info("Запрос на откат отчета от пользователя [{}] в локации [{}]",
                user.getName(),
                user.getLocation().getName());

        sheetsReportService.rollbackFullReport(user, dto.reportDate());
        reportSubmissionService.rollbackReport(user.getId());
        return ResponseEntity.ok(Map.of("message", "Откат данных выполнен успешно"));
    }

    @GetMapping("/status")
    public ResponseEntity<ReportStatusDto> reportStatus(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        ReportStatusDto status = reportSubmissionService.getStatus(user.getId());
        return ResponseEntity.ok(status);
    }
}
