package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.SheetsId;
import com.levita.levita_monitoring.integration.SheetsRanges;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;


@Configuration
public class TableRangesConfig {

    @Value("${google.sheets.credentials.file.path}")
    private String credentialsForFirstTable;

    @Value("${google.sheets.credentials2.file.path}")
    private String credentialsForSecondTable;

    @Bean
    public List<SpreadsheetConfig> spreadsheetConfig(){
        return Arrays.asList(
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        credentialsForFirstTable,
                        Arrays.asList(SheetsRanges.FIRST_VALUE_TEST,
                                SheetsRanges.SECOND_VALUE_TEST,
                                SheetsRanges.THIRD_VALUE_TEST,
                                SheetsRanges.FOURTH_VALUE_TEST,
                                SheetsRanges.FIFTH_VALUE_TEST
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.SECOND_TABLE.getPropertyKey(),
                        credentialsForSecondTable,
                        Arrays.asList(SheetsRanges.FIRST_VALUE_TEST2,
                                SheetsRanges.SECOND_VALUE_TEST2,
                                SheetsRanges.THIRD_VALUE_TEST2,
                                SheetsRanges.FOURTH_VALUE_TEST2,
                                SheetsRanges.FIFTH_VALUE_TEST2
                        )
                )
        );
    }
}
