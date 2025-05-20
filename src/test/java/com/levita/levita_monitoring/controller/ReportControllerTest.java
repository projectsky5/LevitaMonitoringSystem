package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.controller.ReportController;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.dto.RollbackReportDto;
import com.levita.levita_monitoring.dto.FullReportDto.ShiftReportDto;
import com.levita.levita_monitoring.dto.FullReportDto.TrialReportDto;
import com.levita.levita_monitoring.dto.FullReportDto.CurrentReportDto;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.security.CustomUserDetails;
import com.levita.levita_monitoring.service.ReportSubmissionService;
import com.levita.levita_monitoring.service.SheetsReportService;
import com.levita.levita_monitoring.dto.ReportStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    private SheetsReportService sheetsService;
    private ReportSubmissionService submissionService;
    private ReportController ctrl;

    @BeforeEach
    void init() {
        sheetsService = mock(SheetsReportService.class);
        submissionService = mock(ReportSubmissionService.class);
        ctrl = new ReportController(sheetsService, submissionService);
    }

    @Test
    void submitReport_happyPath() throws IOException {
        ShiftReportDto shift = new ShiftReportDto(
                new BigDecimal("8.0"), new BigDecimal("16.0")
        );
        TrialReportDto trial = new TrialReportDto(
                1, 2, new BigDecimal("10"),
                3, new BigDecimal("20"),
                4, new BigDecimal("30"),
                5, new BigDecimal("40")
        );
        CurrentReportDto current = new CurrentReportDto(
                1, 2, new BigDecimal("5"),
                3, new BigDecimal("6"),
                4, new BigDecimal("7"),
                8, new BigDecimal("9"),
                10, new BigDecimal("11"),
                12, new BigDecimal("13"),
                14, new BigDecimal("15")
        );
        FullReportDto dto = new FullReportDto(
                shift, trial, current,
                List.of(),                // нет операций
                "01.01 - пн"              // валидный reportDate
        );

        Location loc = new Location();
        loc.setName("X");
        User u = new User();
        u.setId(5L);
        u.setName("Bob");
        u.setLocation(loc);
        CustomUserDetails cd = new CustomUserDetails(u);

        ResponseEntity<?> res = ctrl.submitReport(dto, cd);

        verify(sheetsService).updateFullReport(u, dto);
        verify(submissionService).submitReport(5L);

        assertEquals(200, res.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String,String> body = (Map<String,String>) res.getBody();
        assertEquals("Отчет успешно загружен", body.get("message"));
    }

    @Test
    void rollbackReport_happyPath() throws IOException {
        RollbackReportDto rb = new RollbackReportDto("02.02 - чт");

        Location loc = new Location();
        loc.setName("Y");
        User u = new User();
        u.setId(8L);
        u.setLocation(loc);
        CustomUserDetails cd = new CustomUserDetails(u);

        ResponseEntity<?> res = ctrl.rollbackReport(cd, rb);

        verify(sheetsService).rollbackFullReport(u, rb.reportDate());
        verify(submissionService).rollbackReport(8L);

        assertEquals(200, res.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String,String> body = (Map<String,String>) res.getBody();
        assertEquals("Откат данных выполнен успешно", body.get("message"));
    }

    @Test
    void reportStatus_returnsDto() {
        User u = new User();
        u.setId(77L);
        CustomUserDetails cd = new CustomUserDetails(u);
        ReportStatusDto dto = new ReportStatusDto(null, null, Instant.now());
        when(submissionService.getStatus(77L)).thenReturn(dto);

        ResponseEntity<ReportStatusDto> res = ctrl.reportStatus(cd);

        verify(submissionService).getStatus(77L);
        assertEquals(200, res.getStatusCodeValue());
        assertSame(dto, res.getBody());
    }
}