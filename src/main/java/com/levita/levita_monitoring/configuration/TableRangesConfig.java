package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.enums.SheetsId;
import com.levita.levita_monitoring.integration.enums.SheetsRanges;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;


@Configuration
public class TableRangesConfig {

    @Bean
    public List<SpreadsheetConfig> spreadsheetConfig(){
        return Arrays.asList(
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.CONVERSION_RATE_1,
                                SheetsRanges.CONVERSION_RATE_2,
                                SheetsRanges.CONVERSION_RATE_3,
                                SheetsRanges.CONVERSION_RATE_4,
                                SheetsRanges.CONVERSION_RATE_5,
                                SheetsRanges.CONVERSION_RATE_6,
                                SheetsRanges.CONVERSION_RATE_7,
                                SheetsRanges.CONVERSION_RATE_8,
                                SheetsRanges.CONVERSION_RATE_9
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.SECOND_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.MAIN_SALARY_PART_1,
                                SheetsRanges.MAIN_SALARY_PART_2,
                                SheetsRanges.MAIN_SALARY_PART_3,
                                SheetsRanges.MAIN_SALARY_PART_4,
                                SheetsRanges.MAIN_SALARY_PART_5,
                                SheetsRanges.MAIN_SALARY_PART_6,
                                SheetsRanges.MAIN_SALARY_PART_7,
                                SheetsRanges.MAIN_SALARY_PART_8,
                                SheetsRanges.MAIN_SALARY_PART_9
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.LOCATION_PLAN_1,
                                SheetsRanges.LOCATION_PLAN_2,
                                SheetsRanges.LOCATION_PLAN_3,
                                SheetsRanges.LOCATION_PLAN_4,
                                SheetsRanges.LOCATION_PLAN_5,
                                SheetsRanges.LOCATION_PLAN_6,
                                SheetsRanges.LOCATION_PLAN_7,
                                SheetsRanges.LOCATION_PLAN_8,
                                SheetsRanges.LOCATION_PLAN_9
                        )
                )
        );
    }
}
