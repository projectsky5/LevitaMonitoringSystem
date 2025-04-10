//TODO: Подумать как избавиться от хардкода диапазонов
package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.configuration.SpreadsheetConfig;
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
                        SheetsId.SECOND_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.CONVERSION_RATE_1,
                                SheetsRanges.CONVERSION_RATE_2,
                                SheetsRanges.CONVERSION_RATE_3,
                                SheetsRanges.CONVERSION_RATE_4,
                                SheetsRanges.CONVERSION_RATE_5,
                                SheetsRanges.CONVERSION_RATE_6,
                                SheetsRanges.CONVERSION_RATE_7,
                                SheetsRanges.CONVERSION_RATE_8,
                                SheetsRanges.CONVERSION_RATE_9,
                                SheetsRanges.CONVERSION_RATE_10,
                                SheetsRanges.CONVERSION_RATE_11,
                                SheetsRanges.CONVERSION_RATE_12,
                                SheetsRanges.CONVERSION_RATE_13,
                                SheetsRanges.CONVERSION_RATE_14,
                                SheetsRanges.CONVERSION_RATE_15,
                                SheetsRanges.CONVERSION_RATE_16
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.SECOND_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.PERSONAL_REVENUE_1,
                                SheetsRanges.PERSONAL_REVENUE_2,
                                SheetsRanges.PERSONAL_REVENUE_3,
                                SheetsRanges.PERSONAL_REVENUE_4,
                                SheetsRanges.PERSONAL_REVENUE_5,
                                SheetsRanges.PERSONAL_REVENUE_6,
                                SheetsRanges.PERSONAL_REVENUE_7,
                                SheetsRanges.PERSONAL_REVENUE_8,
                                SheetsRanges.PERSONAL_REVENUE_9,
                                SheetsRanges.PERSONAL_REVENUE_10,
                                SheetsRanges.PERSONAL_REVENUE_11,
                                SheetsRanges.PERSONAL_REVENUE_12,
                                SheetsRanges.PERSONAL_REVENUE_13,
                                SheetsRanges.PERSONAL_REVENUE_14,
                                SheetsRanges.PERSONAL_REVENUE_15,
                                SheetsRanges.PERSONAL_REVENUE_16
                        )
                ),
//                new SpreadsheetConfig(
//                        SheetsId.SECOND_TABLE.getPropertyKey(),
//                        Arrays.asList(
//                                SheetsRanges.MAIN_SALARY_PART_1,
//                                SheetsRanges.MAIN_SALARY_PART_2,
//                                SheetsRanges.MAIN_SALARY_PART_3,
//                                SheetsRanges.MAIN_SALARY_PART_4,
//                                SheetsRanges.MAIN_SALARY_PART_5,
//                                SheetsRanges.MAIN_SALARY_PART_6,
//                                SheetsRanges.MAIN_SALARY_PART_7,
//                                SheetsRanges.MAIN_SALARY_PART_8,
//                                SheetsRanges.MAIN_SALARY_PART_9
//                        )
//                ),
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
                ),
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.ACTUAL_INCOME_1,
                                SheetsRanges.ACTUAL_INCOME_2,
                                SheetsRanges.ACTUAL_INCOME_3,
                                SheetsRanges.ACTUAL_INCOME_4,
                                SheetsRanges.ACTUAL_INCOME_5,
                                SheetsRanges.ACTUAL_INCOME_6,
                                SheetsRanges.ACTUAL_INCOME_7,
                                SheetsRanges.ACTUAL_INCOME_8,
                                SheetsRanges.ACTUAL_INCOME_9
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.MAX_DAILY_REVENUE_1,
                                SheetsRanges.MAX_DAILY_REVENUE_2,
                                SheetsRanges.MAX_DAILY_REVENUE_3,
                                SheetsRanges.MAX_DAILY_REVENUE_4,
                                SheetsRanges.MAX_DAILY_REVENUE_5,
                                SheetsRanges.MAX_DAILY_REVENUE_6,
                                SheetsRanges.MAX_DAILY_REVENUE_7,
                                SheetsRanges.MAX_DAILY_REVENUE_8,
                                SheetsRanges.MAX_DAILY_REVENUE_9
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.PLAN_COMPLETION_PERCENT_1,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_2,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_3,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_4,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_5,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_6,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_7,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_8,
                                SheetsRanges.PLAN_COMPLETION_PERCENT_9
                        )
                ),
                new SpreadsheetConfig(
                        SheetsId.FIRST_TABLE.getPropertyKey(),
                        Arrays.asList(
                                SheetsRanges.REMAINING_TO_PLAN_1,
                                SheetsRanges.REMAINING_TO_PLAN_2,
                                SheetsRanges.REMAINING_TO_PLAN_3,
                                SheetsRanges.REMAINING_TO_PLAN_4,
                                SheetsRanges.REMAINING_TO_PLAN_5,
                                SheetsRanges.REMAINING_TO_PLAN_6,
                                SheetsRanges.REMAINING_TO_PLAN_7,
                                SheetsRanges.REMAINING_TO_PLAN_8,
                                SheetsRanges.REMAINING_TO_PLAN_9
                        )
                )

        );
    }
}
