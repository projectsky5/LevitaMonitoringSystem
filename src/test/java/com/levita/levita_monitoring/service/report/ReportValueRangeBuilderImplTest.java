package com.levita.levita_monitoring.service.report;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.levita.levita_monitoring.configuration.sheet_reports.*;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportValueRangeBuilderImplTest {

    @Mock
    private YamlConfigLoader yaml;

    @Mock
    private SheetsClient sheetsClient;

    @InjectMocks
    private ReportValueRangeBuilderImpl builder;

    private SheetNamesDescriptor sheetNames;
    private ShiftColumnsDescriptor shiftCols;
    private DateColumnsDescriptor dateCols;
    private TrialColumnsDescriptor trialCols;
    private CurrentColumnsDescriptor currentCols;

    @BeforeEach
    void setUp() {
        sheetNames = new SheetNamesDescriptor();
        sheetNames.setShift("ShiftSheet");
        sheetNames.setTrial("TrialSheet");
        sheetNames.setCurrent("CurrentSheet");

        shiftCols = new ShiftColumnsDescriptor();
        dateCols  = new DateColumnsDescriptor();
        trialCols = new TrialColumnsDescriptor();
        currentCols = new CurrentColumnsDescriptor();

        when(yaml.getSheetNamesDescriptor()).thenReturn(sheetNames);
        when(yaml.getShiftColumnsDescriptor()).thenReturn(shiftCols);
        when(yaml.getDateColumnsDescriptor()).thenReturn(dateCols);
        when(yaml.getTrialColumnsDescriptor()).thenReturn(trialCols);
        when(yaml.getCurrentColumnsDescriptor()).thenReturn(currentCols);
    }

    @Test
    void buildShiftValueRange_happyPath() throws IOException {
        shiftCols.setShiftColumns(Map.of("Loc", "B"));
        when(sheetsClient.getValues("'ShiftSheet'!B3:B"))
                .thenReturn(List.of(
                        List.of("2025-05-14"), List.of("foo")
                ));

        FullReportDto.ShiftReportDto shift = new FullReportDto.ShiftReportDto(
                new BigDecimal("8.0"), new BigDecimal("16.0")
        );
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("loc");
        User user = new User();
        user.setName("Bob");
        user.setLocation(loc);

        ValueRange vr = builder.buildShiftValueRange(shift, user, "2025-05-14");

        assertEquals("'ShiftSheet'!C3:E3", vr.getRange());
        assertEquals(List.of(
                List.of(new BigDecimal("8.0"), new BigDecimal("16.0"), "Bob")
        ), vr.getValues());
    }

    @Test
    void buildShiftValueRange_dateNotFound_throws() throws IOException {
        shiftCols.setShiftColumns(Map.of("X", "B"));
        when(sheetsClient.getValues(anyString()))
                .thenReturn(List.of(List.of("2025-05-13")));
        FullReportDto.ShiftReportDto shift = new FullReportDto.ShiftReportDto(
                BigDecimal.ONE, BigDecimal.TEN
        );
        User u = new User();
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("x");
        u.setLocation(loc);

        assertThrows(IllegalArgumentException.class,
                () -> builder.buildShiftValueRange(shift, u, "2025-05-14")
        );
    }

    @Test
    void buildTrialValueRange_happyPath() throws IOException {
        String key = "Alice (Loc)";
        TrialColumnsDescriptor.Entry entry = new TrialColumnsDescriptor.Entry();
        entry.setSheet("TrialSheet");
        entry.setRange("D:F");
        trialCols.setTrialColumns(Map.of(key, entry));
        dateCols.setDateColumns(Map.of("TrialSheet", "C"));

        when(sheetsClient.getValues("'TrialSheet'!C8:C"))
                .thenReturn(List.of(List.of("foo"), List.of("2025-05-14")));

        FullReportDto.TrialReportDto t = new FullReportDto.TrialReportDto(
                1, 2, new BigDecimal("10"),
                3, new BigDecimal("20"),
                4, new BigDecimal("30"),
                5, new BigDecimal("40")
        );
        User u = new User();
        u.setName("Alice");
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("loc");
        u.setLocation(loc);

        ValueRange vr = builder.buildTrialValueRange(t, u, "2025-05-14");

        assertEquals("'TrialSheet'!D9:F9", vr.getRange());
        assertEquals(List.of(
                List.of(1, 2, new BigDecimal("10"),
                        3, new BigDecimal("20"),
                        4, new BigDecimal("30"),
                        5, new BigDecimal("40"))
        ), vr.getValues());
    }

    @Test
    void buildCurrentValueRange_happyPath() throws IOException {
        String key = "Carol (Loc)";
        CurrentColumnsDescriptor.Entry ce = new CurrentColumnsDescriptor.Entry();
        ce.setSheet("CurrentSheet");
        ce.setRange("G:H");
        currentCols.setCurrentColumns(Map.of(key, ce));
        dateCols.setDateColumns(Map.of("CurrentSheet", "A"));

        when(sheetsClient.getValues("'CurrentSheet'!A6:A"))
                .thenReturn(List.of(List.of("2025-05-14")));

        FullReportDto.CurrentReportDto c = new FullReportDto.CurrentReportDto(
                1,2,new BigDecimal("5"),
                3,new BigDecimal("6"),
                4,new BigDecimal("7"),
                8,new BigDecimal("9"),
                10,new BigDecimal("11"),
                12,new BigDecimal("13"),
                14,new BigDecimal("15")
        );
        User u = new User(); u.setName("Carol");
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("loc");
        u.setLocation(loc);

        ValueRange vr = builder.buildCurrentValueRange(c, u, "2025-05-14");

        assertEquals("'CurrentSheet'!G6:H6", vr.getRange());
        assertEquals(List.of(List.of(
                1,2,new BigDecimal("5"),
                3,new BigDecimal("6"),
                4,new BigDecimal("7"),
                8,new BigDecimal("9"),
                10,new BigDecimal("11"),
                12,new BigDecimal("13"),
                14,new BigDecimal("15")
        )), vr.getValues());
    }

    @Test
    void buildClearShiftValueRange_happyPath() throws IOException {
        shiftCols.setShiftColumns(Map.of("Loc", "B"));
        when(sheetsClient.getValues("'ShiftSheet'!B3:B"))
                .thenReturn(List.of(List.of("2025-05-14")));
        User u = new User();
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("loc");
        u.setLocation(loc);

        ValueRange vr = builder.buildClearShiftValueRange(u, "2025-05-14");
        assertEquals("'ShiftSheet'!C3:D3", vr.getRange());
        assertEquals(List.of(List.of("", "")), vr.getValues());
    }

    @Test
    void buildClearTrialValueRange_happyPath() throws IOException {
        String key = "Dave (Loc)";
        TrialColumnsDescriptor.Entry e = new TrialColumnsDescriptor.Entry();
        e.setSheet("TrialSheet");
        e.setRange("D:F");
        trialCols.setTrialColumns(Map.of(key, e));
        dateCols.setDateColumns(Map.of("TrialSheet", "C"));
        when(sheetsClient.getValues("'TrialSheet'!C8:C"))
                .thenReturn(List.of(List.of("2025-05-14")));

        User u = new User();
        u.setName("Dave");
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("loc");
        u.setLocation(loc);

        ValueRange vr = builder.buildClearTrialValueRange(u, "2025-05-14");
        assertEquals("'TrialSheet'!D8:F8", vr.getRange());
        assertEquals(List.of(List.of("", "", "")), vr.getValues());
    }

    @Test
    void buildClearCurrentValueRange_happyPath() throws IOException {
        String key = "Eve (Loc)";
        CurrentColumnsDescriptor.Entry e = new CurrentColumnsDescriptor.Entry();
        e.setSheet("CurrentSheet");
        e.setRange("X:Z");
        currentCols.setCurrentColumns(Map.of(key, e));
        dateCols.setDateColumns(Map.of("CurrentSheet", "A"));
        when(sheetsClient.getValues("'CurrentSheet'!A6:A"))
                .thenReturn(List.of(List.of("2025-05-14")));

        User u = new User();
        u.setName("Eve");
        Location loc = mock(Location.class);
        when(loc.getName()).thenReturn("loc");
        u.setLocation(loc);

        ValueRange vr = builder.buildClearCurrentValueRange(u, "2025-05-14");
        assertEquals("'CurrentSheet'!X6:Z6", vr.getRange());
        assertEquals(List.of(List.of("", "", "")), vr.getValues());
    }

    @Test
    void updateDefaultValues_delegatesToClient() throws IOException {
        ValueRange vr = new ValueRange()
                .setRange("Sheet!A1:B1")
                .setValues(List.of(List.of(1, 2)));
        builder.updateDefaultValues(vr);
        verify(sheetsClient, times(1))
                .updateValues("Sheet!A1:B1", List.of(List.of(1,2)));
    }
}