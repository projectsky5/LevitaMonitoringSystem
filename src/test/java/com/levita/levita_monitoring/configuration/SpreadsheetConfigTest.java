package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SpreadsheetConfigTest {

    @Test
    void gettersAndSetters_workCorrectly() {
        RangeDescriptor rd = new RangeDescriptor(5, "A1", "CAT", "LBL");
        SpreadsheetConfig cfg = new SpreadsheetConfig();
        cfg.setSpreadsheetId("sheet123");
        cfg.setRanges(List.of(rd));

        assertThat(cfg.getSpreadsheetId()).isEqualTo("sheet123");
        assertThat(cfg.getRanges()).containsExactly(rd);
    }

    @Test
    void constructorAndToString() {
        RangeDescriptor rd1 = new RangeDescriptor(1, "R1", "X", null);
        RangeDescriptor rd2 = new RangeDescriptor(2, "R2", "Y", "L2");
        SpreadsheetConfig cfg = new SpreadsheetConfig("ID42", List.of(rd1, rd2));

        String str = cfg.toString();
        assertThat(str)
                .contains("spreadsheetId=ID42")
                .contains("ranges=[")
                .contains("RangeDescriptor"); // убедиться, что toString включает вложенные дескрипторы
    }

    @Test
    void equalsAndHashCode_consistency() {
        RangeDescriptor a = new RangeDescriptor(1, "A", "C", null);
        RangeDescriptor b = new RangeDescriptor(2, "B", "D", "L");
        SpreadsheetConfig cfg1 = new SpreadsheetConfig("ID", List.of(a, b));
        SpreadsheetConfig cfg2 = new SpreadsheetConfig("ID", List.of(a, b));
        SpreadsheetConfig cfg3 = new SpreadsheetConfig("OTHER", List.of(a, b));

        // reflexive
        assertThat(cfg1).isEqualTo(cfg1);
        // symmetric
        assertThat(cfg1).isEqualTo(cfg2);
        assertThat(cfg2).isEqualTo(cfg1);
        // transitive
        SpreadsheetConfig cfg2Copy = new SpreadsheetConfig("ID", List.of(a, b));
        assertThat(cfg1).isEqualTo(cfg2).isEqualTo(cfg2Copy);

        // unequal
        assertThat(cfg1).isNotEqualTo(cfg3);
        assertThat(cfg1.hashCode()).isEqualTo(cfg2.hashCode());
        assertThat(cfg1.hashCode()).isNotEqualTo(cfg3.hashCode());
    }

    @Test
    void equals_nullAndDifferentType() {
        SpreadsheetConfig cfg = new SpreadsheetConfig("X", List.of());
        assertThat(cfg).isNotEqualTo(null);
        assertThat(cfg).isNotEqualTo("some string");
    }
}