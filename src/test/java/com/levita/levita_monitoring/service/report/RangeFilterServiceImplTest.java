package com.levita.levita_monitoring.service.report;

import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RangeFilterServiceImplTest {

    private RangeFilterServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RangeFilterServiceImpl();
    }

    @Test
    void filterByCategory_includesOnlyMatchingDescriptors() {
        RangeDescriptor rd1 = new RangeDescriptor(1, "0-5", "CatA", "Label1");
        RangeDescriptor rd2 = new RangeDescriptor(2, "5-10", "cata", "Label2");
        RangeDescriptor rd3 = new RangeDescriptor(3, "10-15", "CatB", "Label3");
        SpreadsheetConfig config = new SpreadsheetConfig("sheet1", List.of(rd1, rd2, rd3));

        List<RangeDescriptor> result = service.filterByCategory(config, "CATA");

        assertThat(result)
                .hasSize(2)
                .containsExactly(rd1, rd2);
    }

    @Test
    void filterByCategory_noMatches_returnsEmptyList() {
        RangeDescriptor rd = new RangeDescriptor(1, "0-5", "X", "L");
        SpreadsheetConfig config = new SpreadsheetConfig("sheet2", List.of(rd));
        List<RangeDescriptor> result = service.filterByCategory(config, "Y");

        assertThat(result).isEmpty();
    }

    @Test
    void filterByCategory_emptyRanges_returnsEmptyList() {
        SpreadsheetConfig config = new SpreadsheetConfig("sheet3", List.of());
        List<RangeDescriptor> result = service.filterByCategory(config, "anything");

        assertThat(result).isEmpty();
    }

    @Test
    void filterExcluding_excludesExactCategories_onlyCaseSensitive() {
        RangeDescriptor rd1 = new RangeDescriptor(1, "0-5", "CatA", "Label1");
        RangeDescriptor rd2 = new RangeDescriptor(2, "5-10", "catb", "Label2");
        SpreadsheetConfig config = new SpreadsheetConfig("sheet4", List.of(rd1, rd2));

        List<RangeDescriptor> result = service.filterExcluding(config, List.of("CatA"));

        assertThat(result)
                .hasSize(1)
                .containsExactly(rd2);
    }

    @Test
    void filterExcluding_noExclusions_returnsAll(){
        RangeDescriptor rd = new RangeDescriptor(1, "0-100", "Any", "L");
        SpreadsheetConfig config = new SpreadsheetConfig("sheet5", List.of(rd));

        List<RangeDescriptor> result = service.filterExcluding(config, List.of());

        assertThat(result).containsExactly(rd);
    }

    @Test
    void filterExcluding_emptyRanges_returnsEmptyList() {
        SpreadsheetConfig config = new SpreadsheetConfig("sheet6", List.of());

        List<RangeDescriptor> result = service.filterExcluding(config, List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void filterByCategory_nullConfig_throwsNullPointerException() {
        assertThatThrownBy(() -> service.filterByCategory(null, "anything"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void filterByCategory_nullCategory_returnsEmptyList() {
        RangeDescriptor rd = new RangeDescriptor(1, "0-5", "Cat", "L");
        SpreadsheetConfig config = new SpreadsheetConfig("sheet7", List.of(rd));

        List<RangeDescriptor> result = service.filterByCategory(config, null);

        assertThat(result).isEmpty();
    }

    @Test
    void filterByCategory_nullRangesInConfig_throwsNullPointerException() {
        SpreadsheetConfig config = new SpreadsheetConfig("sheet8", null);
        assertThatThrownBy(() -> service.filterByCategory(config, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void filterExcluding_nullConfig_throwsNullPointerException() {
        assertThatThrownBy(() -> service.filterExcluding(null, List.of("X")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void filterExcluding_nullExcludedCategories_throwsNullPointerException() {
        RangeDescriptor rd = new RangeDescriptor(1, "0-5", "Cat", "L");
        SpreadsheetConfig config = new SpreadsheetConfig("sheet9", List.of(rd));

        assertThatThrownBy(() -> service.filterExcluding(config, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void filterExcluding_nullRangesInConfig_throwsNullPointerException() {
        SpreadsheetConfig config = new SpreadsheetConfig("sheet10", null);
        assertThatThrownBy(() -> service.filterExcluding(config, null))
            .isInstanceOf(NullPointerException.class);
    }
}