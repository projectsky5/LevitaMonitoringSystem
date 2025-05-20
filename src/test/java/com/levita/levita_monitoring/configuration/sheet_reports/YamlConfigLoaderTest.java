package com.levita.levita_monitoring.configuration.sheet_reports;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class YamlConfigLoaderTest {

    @Autowired
    private YamlConfigLoader loader;

    @Test
    void loadAll_shouldPopulateAllDescriptors() {
        assertThat(loader.getSheetNamesDescriptor())
                .as("SheetNamesDescriptor must be loaded")
                .isNotNull();

        assertThat(loader.getShiftColumnsDescriptor())
                .as("ShiftColumnsDescriptor must be loaded")
                .isNotNull();

        assertThat(loader.getDateColumnsDescriptor())
                .as("DateColumnsDescriptor must be loaded")
                .isNotNull();

        assertThat(loader.getCurrentColumnsDescriptor())
                .as("CurrentColumnsDescriptor must be loaded")
                .isNotNull();

        assertThat(loader.getTrialColumnsDescriptor())
                .as("TrialColumnsDescriptor must be loaded")
                .isNotNull();
    }
}