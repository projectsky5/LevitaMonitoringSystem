package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.service.KpiDataService;
import com.levita.levita_monitoring.service.SheetsParserService;
import com.levita.levita_monitoring.service.report.RangeFilterService;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheetsParserServiceTest {

    @Mock private RangeFilterService rangeFilter;
    @Mock private KpiDataService kpiDataService;
    @Mock private SheetsClient sheetsClient;
    @Mock private ExecutorService executor;

    @Test
    void runSheetsParser_processesLocationsUsersAndKpiData() throws Exception {
        SpreadsheetConfig config = mock(SpreadsheetConfig.class);
        when(config.getSpreadsheetId()).thenReturn("sheet1");

        RangeDescriptor descLoc  = mock(RangeDescriptor.class);
        RangeDescriptor descUser = mock(RangeDescriptor.class);
        RangeDescriptor descKpi  = mock(RangeDescriptor.class);

        when(rangeFilter.filterByCategory(config, "LOCATIONS")).thenReturn(List.of(descLoc));
        when(rangeFilter.filterByCategory(config, "USERS")).thenReturn(List.of(descUser));
        when(rangeFilter.filterExcluding(eq(config), eq(List.of("LOCATIONS","USERS"))))
                .thenReturn(List.of(descKpi));

        when(descLoc.range()).thenReturn("rLoc");
        when(descUser.range()).thenReturn("rUser");
        when(descKpi.range()).thenReturn("rKpi");

        when(sheetsClient.batchGetValues("sheet1", List.of("rLoc")))
                .thenReturn(List.of(new com.google.api.services.sheets.v4.model.ValueRange()
                        .setValues(List.of(List.of("valLoc")))));
        when(sheetsClient.batchGetValues("sheet1", List.of("rUser")))
                .thenReturn(List.of(new com.google.api.services.sheets.v4.model.ValueRange()
                        .setValues(List.of(List.of("valUser")))));
        when(sheetsClient.batchGetValues("sheet1", List.of("rKpi")))
                .thenReturn(List.of(new com.google.api.services.sheets.v4.model.ValueRange()
                        .setValues(List.of(List.of("valKpi")))));

        when(executor.submit(any(Runnable.class)))
                .thenAnswer(inv -> {
                    Runnable task = inv.getArgument(0);
                    task.run();
                    return CompletableFuture.completedFuture(null);
                });

        SheetsParserService service = new SheetsParserService(
                List.of(config),
                kpiDataService,
                sheetsClient,
                executor,
                rangeFilter
        );

        service.runSheetsParser();

        verify(kpiDataService).saveDataFromSheets(descLoc,  "valLoc");
        verify(kpiDataService).saveDataFromSheets(descUser, "valUser");
        verify(kpiDataService).saveDataFromSheets(descKpi,  "valKpi");
        verifyNoMoreInteractions(kpiDataService);
    }

    @Test
    void processBatch_skipsEmptyAndInvokesSave() throws IOException {
        RangeDescriptor good = mock(RangeDescriptor.class);
        RangeDescriptor empty = mock(RangeDescriptor.class);
        when(good.range()).thenReturn("goodRange");
        when(empty.range()).thenReturn("emptyRange");

        var vrGood = new com.google.api.services.sheets.v4.model.ValueRange()
                .setValues(List.of(List.of("X")));
        var vrEmpty = new com.google.api.services.sheets.v4.model.ValueRange()
                .setValues(List.of());
        when(sheetsClient.batchGetValues(eq("sheet1"), eq(List.of("goodRange","emptyRange"))))
                .thenReturn(List.of(vrGood, vrEmpty));

        SheetsParserService service = new SheetsParserService(
                List.of(mock(SpreadsheetConfig.class)),
                kpiDataService,
                sheetsClient,
                executor,
                rangeFilter
        );

        service.processBatch("sheet1", List.of(good, empty));

        verify(kpiDataService).saveDataFromSheets(good, "X");
        verifyNoMoreInteractions(kpiDataService);
    }
}
