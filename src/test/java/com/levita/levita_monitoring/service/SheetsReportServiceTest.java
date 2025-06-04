package com.levita.levita_monitoring.service;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.service.report.OperationsService;
import com.levita.levita_monitoring.service.report.ReportValueRangeBuilder;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheetsReportServiceTest {

    @Mock private SheetsClient sheetsClient;
    @Mock private ReportValueRangeBuilder builder;
    @Mock private OperationsService operationsService;

    @InjectMocks private SheetsReportService service;

    private User user;
    private FullReportDto dto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Bob");

        FullReportDto.ShiftReportDto shift = new FullReportDto.ShiftReportDto(
                new BigDecimal("1"), new BigDecimal("2"));
        FullReportDto.TrialReportDto trial = new FullReportDto.TrialReportDto(
                0,0,BigDecimal.ZERO,0,BigDecimal.ZERO,0,BigDecimal.ZERO,0,BigDecimal.ZERO);
        FullReportDto.CurrentReportDto current = new FullReportDto.CurrentReportDto(
                0,0,BigDecimal.ZERO,0,BigDecimal.ZERO,0,BigDecimal.ZERO,
                0,BigDecimal.ZERO,0,BigDecimal.ZERO,0,BigDecimal.ZERO,0,BigDecimal.ZERO);
        dto = new FullReportDto(
                shift, trial, current,
                List.of(),               // no operations
                "01.01 - пн"             // valid pattern
        );
    }

    @Test
    void updateFullReport_invalidDate_throwsBadRequest() {
        dto = new FullReportDto(
                dto.shift(), dto.trial(), dto.current(), dto.operations(),
                "invalid-date"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateFullReport(user, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Неправильный формат reportDate"));
        verifyNoInteractions(builder, operationsService, sheetsClient);
    }

    @Test
    void updateFullReport_happyPath_callsBuilderAndOperations() throws IOException {
        ValueRange vr1 = new ValueRange().setRange("A1");
        ValueRange vr2 = new ValueRange().setRange("B2");
        ValueRange vr3 = new ValueRange().setRange("C3");

        when(builder.buildShiftValueRange(dto.shift(), user, dto.reportDate())).thenReturn(vr1);
        when(builder.buildTrialValueRange(dto.trial(), user, dto.reportDate())).thenReturn(vr2);
        when(builder.buildCurrentValueRange(dto.current(), user, dto.reportDate())).thenReturn(vr3);

        service.updateFullReport(user, dto);

        InOrder inOrder = inOrder(builder, sheetsClient, operationsService);
        inOrder.verify(builder).buildShiftValueRange(dto.shift(), user, dto.reportDate());
        inOrder.verify(builder).buildTrialValueRange(dto.trial(), user, dto.reportDate());
        inOrder.verify(builder).buildCurrentValueRange(dto.current(), user, dto.reportDate());

        inOrder.verify(sheetsClient).updateValues("A1", vr1.getValues());
        inOrder.verify(sheetsClient).updateValues("B2", vr2.getValues());
        inOrder.verify(sheetsClient).updateValues("C3", vr3.getValues());

        inOrder.verify(builder).updateDefaultValues(vr1);
        inOrder.verify(builder).updateDefaultValues(vr2);
        inOrder.verify(builder).updateDefaultValues(vr3);
        // 4. операции
        inOrder.verify(operationsService).saveOperations(dto.operations(), user, dto.reportDate());

        verifyNoMoreInteractions(builder, sheetsClient, operationsService);
    }

    @Test
    void rollbackFullReport_invalidDate_throwsBadRequest() {
        assertThrows(ResponseStatusException.class,
                () -> service.rollbackFullReport(user, "bad-date"));
        verifyNoInteractions(builder, operationsService, sheetsClient);
    }

    @Test
    void rollbackFullReport_happyPath_callsClearAndRollback() throws IOException {
        ValueRange cr1 = new ValueRange().setRange("S1").setValues(List.of(List.of("x")));
        ValueRange cr2 = new ValueRange().setRange("S2").setValues(List.of(List.of("y")));
        ValueRange cr3 = new ValueRange().setRange("S3").setValues(List.of(List.of("z")));

        when(builder.buildClearShiftValueRange(user, dto.reportDate())).thenReturn(cr1);
        when(builder.buildClearTrialValueRange(user, dto.reportDate())).thenReturn(cr2);
        when(builder.buildClearCurrentValueRange(user, dto.reportDate())).thenReturn(cr3);

        service.rollbackFullReport(user, dto.reportDate());

        verify(sheetsClient).updateValues(cr1.getRange(), cr1.getValues());
        verify(sheetsClient).updateValues(cr2.getRange(), cr2.getValues());
        verify(sheetsClient).updateValues(cr3.getRange(), cr3.getValues());

        verify(operationsService).rollbackOperations(user, dto.reportDate());
        verifyNoMoreInteractions(builder, operationsService, sheetsClient);
    }
}