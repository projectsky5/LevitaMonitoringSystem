package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RangeDescriptorLoaderTest {

    @Test
    void spreadsheetConfigFromYaml_parsesTemplateAndExplicitRanges() throws Exception {
        RangeDescriptorLoader loader = new RangeDescriptorLoader();

        List<SpreadsheetConfig> configs = loader.spreadsheetConfigFromYaml();

        // Должен быть только один SpreadsheetConfig из тестового YAML
        assertThat(configs)
                .hasSize(1);

        SpreadsheetConfig cfg = configs.get(0);
        assertThat(cfg.getSpreadsheetId())
                .isEqualTo("spreadsheet1");

        List<RangeDescriptor> descriptors = cfg.getRanges();
        assertThat(descriptors)
                .hasSize(3)
                .extracting(RangeDescriptor::range)
                .containsExactly("A1", "A2", "B1");

        RangeDescriptor d1 = descriptors.get(0);
        assertThat(d1.getId()).isEqualTo(1);
        assertThat(d1.getCategory()).isEqualTo("USERS");
        assertThat(d1.getLabel()).isNull();

        RangeDescriptor d2 = descriptors.get(1);
        assertThat(d2.getId()).isEqualTo(2);
        assertThat(d2.getCategory()).isEqualTo("USERS");

        RangeDescriptor d3 = descriptors.get(2);
        assertThat(d3.getId()).isEqualTo(10);
        assertThat(d3.getCategory()).isEqualTo("LOCATIONS");
        assertThat(d3.getLabel()).isEqualTo("lbl");
    }
}