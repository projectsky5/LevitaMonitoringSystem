package com.levita.levita_monitoring.configuration;

import com.levita.levita_monitoring.integration.enums.SheetsId;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Configuration
public class TableRangesConfig {

    @Bean
    public List<SpreadsheetConfig> spreadsheetConfig(){
        List<SpreadsheetConfig> configs = new ArrayList<>();

        List<RangeDescriptor> planCompletionPercent = IntStream.rangeClosed(1, 9)
                .mapToObj(i -> new RangeDescriptor("PLAN_COMPLETION_PERCENT", i, String.format("'План-Факт %d'!BJ4:BM4", i)))
                .toList();

        List<RangeDescriptor> actualIncome = IntStream.rangeClosed(1, 9)
                .mapToObj(i -> new RangeDescriptor("ACTUAL_INCOME",i,String.format("'План-Факт %d'!BF4:BH4", i)))
                .toList();

        List<RangeDescriptor> locationPlan = IntStream.rangeClosed(1, 9)
                .mapToObj(i -> new RangeDescriptor("LOCATION_PLAN",i,String.format("'План-Факт %d'!BF3:BH3", i)))
                .toList();

        List<RangeDescriptor> maxDailyRevenue = IntStream.rangeClosed(1, 9)
                .mapToObj(i -> new RangeDescriptor("MAX_DAILY_REVENUE",i,String.format("'План-Факт %d'!BL100:BL100", i)))
                .toList();

        List<RangeDescriptor> remainingToPlan = IntStream.rangeClosed(1, 9)
                .mapToObj(i -> new RangeDescriptor("REMAINING_TO_PLAN",i,String.format("'План-Факт %d'!BK50:BK50", i)))
                .toList();

        List<RangeDescriptor> conversionRate = List.of(
                new RangeDescriptor("CONVERSION_RATE", 1, "'Пробные (команда Кати)'!D51:D51"),
                new RangeDescriptor("CONVERSION_RATE", 2, "'Пробные (команда Кати)'!D52:D52"),
                new RangeDescriptor("CONVERSION_RATE", 3, "'Пробные (команда Кати)'!D54:D54"),
                new RangeDescriptor("CONVERSION_RATE", 4, "'Пробные (команда Кати)'!D55:D55"),
                new RangeDescriptor("CONVERSION_RATE", 5, "'Пробные (команда Кати)'!D60:D60"),
                new RangeDescriptor("CONVERSION_RATE", 6, "'Пробные (команда Кати)'!D61:D61"),
                new RangeDescriptor("CONVERSION_RATE", 7, "'Пробные (команда Кати)'!D63:D63"),
                new RangeDescriptor("CONVERSION_RATE", 8, "'Пробные (команда Кати)'!D64:D64"),
                new RangeDescriptor("CONVERSION_RATE", 9, "'Пробные (команда Алины)'!D51:D51"),
                new RangeDescriptor("CONVERSION_RATE", 10, "'Пробные (команда Алины)'!D52:D52"),
                new RangeDescriptor("CONVERSION_RATE", 11, "'Пробные (команда Алины)'!D53:D53"),
                new RangeDescriptor("CONVERSION_RATE", 12, "'Пробные (команда Алины)'!D54:D54"),
                new RangeDescriptor("CONVERSION_RATE", 13, "'Пробные (команда Алины)'!D56:D56"),
                new RangeDescriptor("CONVERSION_RATE", 14, "'Пробные (команда Алины)'!D55:D55"),
                new RangeDescriptor("CONVERSION_RATE", 15, "'Пробные (команда Алины)'!D57:D57"),
                new RangeDescriptor("CONVERSION_RATE", 16, "'Пробные (команда Алины)'!D58:D58")
        );

        List<RangeDescriptor> personalRevenue = List.of(
                new RangeDescriptor("CONVERSION_RATE", 1, "'Пробные (команда Кати)'!J51:J51"),
                new RangeDescriptor("PERSONAL_REVENUE", 2, "'Пробные (команда Кати)'!J52:J52"),
                new RangeDescriptor("PERSONAL_REVENUE", 3, "'Пробные (команда Кати)'!J54:J54"),
                new RangeDescriptor("PERSONAL_REVENUE", 4, "'Пробные (команда Кати)'!J55:J55"),
                new RangeDescriptor("PERSONAL_REVENUE", 5, "'Пробные (команда Кати)'!J60:J60"),
                new RangeDescriptor("PERSONAL_REVENUE", 6, "'Пробные (команда Кати)'!J61:J61"),
                new RangeDescriptor("PERSONAL_REVENUE", 7, "'Пробные (команда Кати)'!J63:J63"),
                new RangeDescriptor("PERSONAL_REVENUE", 8, "'Пробные (команда Кати)'!J64:J64"),
                new RangeDescriptor("PERSONAL_REVENUE", 9, "'Пробные (команда Алины)'!J51:J51"),
                new RangeDescriptor("PERSONAL_REVENUE", 10, "'Пробные (команда Алины)'!J52:J52"),
                new RangeDescriptor("PERSONAL_REVENUE", 11, "'Пробные (команда Алины)'!J53:J53"),
                new RangeDescriptor("PERSONAL_REVENUE", 12, "'Пробные (команда Алины)'!J54:J54"),
                new RangeDescriptor("PERSONAL_REVENUE", 13, "'Пробные (команда Алины)'!J56:J56"),
                new RangeDescriptor("PERSONAL_REVENUE", 14, "'Пробные (команда Алины)'!J55:J55"),
                new RangeDescriptor("PERSONAL_REVENUE", 15, "'Пробные (команда Алины)'!J57:J57"),
                new RangeDescriptor("PERSONAL_REVENUE", 16, "'Пробные (команда Алины)'!J58:J58")
        );

        configs.add(new SpreadsheetConfig(SheetsId.FIRST_TABLE.getPropertyKey(), planCompletionPercent));
        configs.add(new SpreadsheetConfig(SheetsId.FIRST_TABLE.getPropertyKey(), actualIncome));
        configs.add(new SpreadsheetConfig(SheetsId.FIRST_TABLE.getPropertyKey(), locationPlan));
        configs.add(new SpreadsheetConfig(SheetsId.FIRST_TABLE.getPropertyKey(), maxDailyRevenue));
        configs.add(new SpreadsheetConfig(SheetsId.FIRST_TABLE.getPropertyKey(), remainingToPlan));
        configs.add(new SpreadsheetConfig(SheetsId.SECOND_TABLE.getPropertyKey(), conversionRate));
        configs.add(new SpreadsheetConfig(SheetsId.SECOND_TABLE.getPropertyKey(), personalRevenue));

        return configs;
    }
}
