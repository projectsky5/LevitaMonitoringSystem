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
                            Arrays.asList(SheetsRanges.CONVERSION_RATE_1,
                                            SheetsRanges.CONVERSION_RATE_2
//                        Arrays.asList(SheetsRanges.FIRST_VALUE_TEST,
//                                SheetsRanges.SECOND_VALUE_TEST,
//                                SheetsRanges.THIRD_VALUE_TEST,
//                                SheetsRanges.FOURTH_VALUE_TEST,
//                                SheetsRanges.FIFTH_VALUE_TEST
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.SECOND_TABLE.getPropertyKey(),
                                    Arrays.asList(SheetsRanges.MAIN_SALARY_PART_1,
                                                    SheetsRanges.MAIN_SALARY_PART_2
//                        Arrays.asList(SheetsRanges.FIRST_VALUE_TEST2,
//                                SheetsRanges.SECOND_VALUE_TEST2,
//                                SheetsRanges.THIRD_VALUE_TEST2,
//                                SheetsRanges.FOURTH_VALUE_TEST2,
//                                SheetsRanges.FIFTH_VALUE_TEST2
                        )
                )
        );
    }
}
