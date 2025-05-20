package com.levita.levita_monitoring.service.report;

import com.levita.levita_monitoring.configuration.sheet_reports.SheetNamesDescriptor;
import com.levita.levita_monitoring.configuration.sheet_reports.DateColumnsDescriptor;
import com.levita.levita_monitoring.configuration.sheet_reports.YamlConfigLoader;
import com.levita.levita_monitoring.dto.FullReportDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.service.sheets.SheetsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperationsServiceImplTest {

    private YamlConfigLoader yamlLoader;
    private SheetNamesDescriptor sheetNamesDescriptor;
    private DateColumnsDescriptor dateColumnsDescriptor;
    private SheetsClient sheetsClient;
    private OperationsServiceImpl service;

    @BeforeEach
    void setUp() {
        yamlLoader = mock(YamlConfigLoader.class);
        sheetNamesDescriptor = mock(SheetNamesDescriptor.class);
        dateColumnsDescriptor = mock(DateColumnsDescriptor.class);
        when(yamlLoader.getSheetNamesDescriptor()).thenReturn(sheetNamesDescriptor);
        when(yamlLoader.getDateColumnsDescriptor()).thenReturn(dateColumnsDescriptor);
        sheetsClient = mock(SheetsClient.class);
        service = new OperationsServiceImpl(yamlLoader, sheetsClient);
    }

    @Test
    void buildCellRange_shouldReturnCorrectFormat() {
        assertThat(service.buildCellRange("MySheet", "D", 10))
                .isEqualTo("'MySheet'!D10");
    }

    @Test
    void buildOperationRow_shouldAssembleRowCorrectly() {
        List<Object> row = service.buildOperationRow("2025-05-14", 123L, "", "cash", "cat", "admin", "comment");
        assertThat(row)
                .containsExactly("2025-05-14", 123L, "", "cash", "cat", "admin", "comment");
    }

    @Test
    void formatAmount_nullInput_returnsNull() {
        assertThat(service.formatAmount(null)).isNull();
    }

    @Test
    void formatAmount_integerBigDecimal_returnsLong() {
        assertThat(service.formatAmount(BigDecimal.valueOf(5000, 0))).isEqualTo(5000L);
    }

    @Test
    void formatAmount_fractionBigDecimal_returnsDouble() {
        assertThat(service.formatAmount(new BigDecimal("12.3400"))).isEqualTo(12.34);
    }

    @Test
    void normalizeLocation_nullOrBlank_returnsAsIs() {
        assertThat(service.normalizeLocation(null)).isNull();
        assertThat(service.normalizeLocation("")).isEmpty();
        assertThat(service.normalizeLocation("   ")).isBlank();
    }

    @Test
    void normalizeLocation_capitalizesRussianString() {
        assertThat(service.normalizeLocation("МОСКВА")).isEqualTo("Москва");
        assertThat(service.normalizeLocation("петербург")).isEqualTo("Петербург");
    }

    @Test
    void findRowsByDate_withMatches_returnsCorrectRowNumbers() throws IOException {
        when(sheetsClient.getValues("'Sheet1'!C2:C")).thenReturn(List.of(
                List.of("2025-05-14"),
                Collections.emptyList(),
                List.of("2025-05-14")
        ));

        List<Integer> result = service.findRowsByDate("Sheet1", "C", 2, "2025-05-14");
        assertThat(result).containsExactly(2, 4);
    }

    @Test
    void findRowsByDate_nullValues_returnsEmptyList() throws IOException {
        when(sheetsClient.getValues("'S'!A1:A")).thenReturn(null);
        assertThat(service.findRowsByDate("S", "A", 1, "2025-01-01")).isEmpty();
    }

    @Test
    void findFirstEmptyRow_nullOrEmptyValues_returnsStartRow() throws IOException {
        when(sheetsClient.getValues("'X'!A5:A")).thenReturn(null);
        assertThat(service.findFirstEmptyRow("X", "A", 5)).isEqualTo(5);

        when(sheetsClient.getValues("'X'!A5:A")).thenReturn(Collections.emptyList());
        assertThat(service.findFirstEmptyRow("X", "A", 5)).isEqualTo(5);
    }

    @Test
    void findFirstEmptyRow_withEmptyRowInMiddle_returnsThatRow() throws IOException {
        when(sheetsClient.getValues("'Y'!B2:B")).thenReturn(List.of(
                List.of("v1"),
                Collections.emptyList(),
                List.of("v2")
        ));
        assertThat(service.findFirstEmptyRow("Y", "B", 2)).isEqualTo(3);
    }

    @Test
    void findFirstEmptyRow_noEmptyRows_returnsNextAfterLast() throws IOException {
        when(sheetsClient.getValues("'Z'!D10:D")).thenReturn(List.of(
                List.of("a"), List.of("b")
        ));
        assertThat(service.findFirstEmptyRow("Z", "D", 10)).isEqualTo(12);
    }

    @Test
    void saveOperations_nullOrEmptyOperations_appendsOnlyDate() throws IOException {
        String rawLocation = "loc";
        String normalized = service.normalizeLocation(rawLocation);
        String sheetName = "OpsSheet";

        when(sheetNamesDescriptor.getOperations()).thenReturn(Map.of(normalized, sheetName));
        when(dateColumnsDescriptor.getDateColumns()).thenReturn(Map.of(sheetName, "C"));
        when(sheetsClient.getValues("'OpsSheet'!C2:C")).thenReturn(null);

        User user = mock(User.class);
        com.levita.levita_monitoring.model.Location loc = mock(com.levita.levita_monitoring.model.Location.class);
        when(user.getLocation()).thenReturn(loc);
        when(loc.getName()).thenReturn(rawLocation);
        when(user.getName()).thenReturn("admin");

        service.saveOperations(null, user, "2025-05-14");
        service.saveOperations(Collections.emptyList(), user, "2025-05-14");

        verify(sheetsClient, times(2))
                .appendRow("'OpsSheet'!C2", List.of("2025-05-14"));
    }

    @Test
    void saveOperations_withOperations_appendsRowsInSequence() throws IOException {
        String rawLocation = "loc";
        String normalized = service.normalizeLocation(rawLocation);
        String sheetName = "OpsSheet";

        when(sheetNamesDescriptor.getOperations()).thenReturn(Map.of(normalized, sheetName));
        when(dateColumnsDescriptor.getDateColumns()).thenReturn(Map.of(sheetName, "C"));
        when(sheetsClient.getValues("'OpsSheet'!C2:C")).thenReturn(null);

        User user = mock(User.class);
        com.levita.levita_monitoring.model.Location loc = mock(com.levita.levita_monitoring.model.Location.class);
        when(user.getLocation()).thenReturn(loc);
        when(loc.getName()).thenReturn(rawLocation);
        when(user.getName()).thenReturn("admin");

        FullReportDto.OperationDto inc = new FullReportDto.OperationDto(
                "Приход", new BigDecimal("100"), "cash", "cat", "comment1");
        FullReportDto.OperationDto exp = new FullReportDto.OperationDto(
                "Расход", new BigDecimal("50"),  "cash", "cat2", "comment2");

        service.saveOperations(List.of(inc, exp), user, "2025-05-14");

        InOrder inOrder = inOrder(sheetsClient);
        inOrder.verify(sheetsClient).appendRow(
                "'OpsSheet'!C2",
                List.of("2025-05-14", 100L, "", "cash", "cat",  "admin", "comment1")
        );
        inOrder.verify(sheetsClient).appendRow(
                "'OpsSheet'!C3",
                List.of("2025-05-14",  "", 50L, "cash", "cat2", "admin", "comment2")
        );
    }

    @Test
    void saveOperations_missingSheet_throwsException() {
        when(sheetNamesDescriptor.getOperations()).thenReturn(Collections.emptyMap());

        User user = mock(User.class);
        com.levita.levita_monitoring.model.Location loc = mock(com.levita.levita_monitoring.model.Location.class);
        when(user.getLocation()).thenReturn(loc);
        when(loc.getName()).thenReturn("unknown");

        assertThatThrownBy(() -> service.saveOperations(List.of(), user, "d"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Не найден лист операций");
    }

    @Test
    void rollbackOperations_withRows_deletesThem() throws IOException {
        String rawLocation = "loc";
        String normalized = service.normalizeLocation(rawLocation);
        String sheetName = "OpsSheet";

        when(sheetNamesDescriptor.getOperations()).thenReturn(Map.of(normalized, sheetName));
        when(dateColumnsDescriptor.getDateColumns()).thenReturn(Map.of(sheetName, "C"));
        when(sheetsClient.getValues("'OpsSheet'!C2:C")).thenReturn(List.of(
                List.of("2025-05-14"),
                List.of("x"),
                List.of("2025-05-14")
        ));

        User user = mock(User.class);
        com.levita.levita_monitoring.model.Location loc = mock(com.levita.levita_monitoring.model.Location.class);
        when(user.getLocation()).thenReturn(loc);
        when(loc.getName()).thenReturn(rawLocation);

        service.rollbackOperations(user, "2025-05-14");

        verify(sheetsClient).deleteRows(sheetName, List.of(2, 4));
    }

    @Test
    void rollbackOperations_noRows_doesNotDelete() throws IOException {
        String rawLocation = "loc";
        String normalized = service.normalizeLocation(rawLocation);
        String sheetName = "OpsSheet";

        when(sheetNamesDescriptor.getOperations()).thenReturn(Map.of(normalized, sheetName));
        when(dateColumnsDescriptor.getDateColumns()).thenReturn(Map.of(sheetName, "C"));
        when(sheetsClient.getValues("'OpsSheet'!C2:C")).thenReturn(Collections.emptyList());

        User user = mock(User.class);
        com.levita.levita_monitoring.model.Location loc = mock(com.levita.levita_monitoring.model.Location.class);
        when(user.getLocation()).thenReturn(loc);
        when(loc.getName()).thenReturn(rawLocation);

        service.rollbackOperations(user, "2025-05-14");

        verify(sheetsClient, never()).deleteRows(any(), any());
    }

    @Test
    void rollbackOperations_missingSheet_throwsException() {
        when(sheetNamesDescriptor.getOperations()).thenReturn(Collections.emptyMap());

        User user = mock(User.class);
        com.levita.levita_monitoring.model.Location loc = mock(com.levita.levita_monitoring.model.Location.class);
        when(user.getLocation()).thenReturn(loc);
        when(loc.getName()).thenReturn("unknown");

        assertThatThrownBy(() -> service.rollbackOperations(user, "2025-05-14"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Не найден лист операций");
    }
}