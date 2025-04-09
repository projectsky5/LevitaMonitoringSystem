//TODO: Убрать хардкод значений
package com.levita.levita_monitoring.integration.enums;

public enum SheetsRanges {

//diploma-test
    CONVERSION_RATE_1("'Лист1'!E2:E2"),
    CONVERSION_RATE_2("'Лист1'!E3:E3"),
    CONVERSION_RATE_3("'Лист1'!E4:E4"),
    CONVERSION_RATE_4("'Лист1'!E5:E5"),
    CONVERSION_RATE_5("'Лист1'!E6:E6"),
    CONVERSION_RATE_6("'Лист1'!E7:E7"),
    CONVERSION_RATE_7("'Лист1'!E8:E8"),
    CONVERSION_RATE_8("'Лист1'!E9:E9"),
    CONVERSION_RATE_9("'Лист1'!E10:E10"),
//    FIRST_VALUE_TEST("'Лист1'!B2:B2"),
//    SECOND_VALUE_TEST("'Лист1'!C5:C5"),
//    THIRD_VALUE_TEST("'Лист1'!D7:D7"),
//    FOURTH_VALUE_TEST("'Лист2'!C5:C5"),
//    FIFTH_VALUE_TEST("'Лист2'!D7:D7"),

//    Diploma-test2
    MAIN_SALARY_PART_1("'Лист2'!F2:F2"),
    MAIN_SALARY_PART_2("'Лист2'!F3:F3"),
    MAIN_SALARY_PART_3("'Лист2'!F4:F4"),
    MAIN_SALARY_PART_4("'Лист2'!F5:F5"),
    MAIN_SALARY_PART_5("'Лист2'!F6:F6"),
    MAIN_SALARY_PART_6("'Лист2'!F7:F7"),
    MAIN_SALARY_PART_7("'Лист2'!F8:F8"),
    MAIN_SALARY_PART_8("'Лист2'!F9:F9"),
    MAIN_SALARY_PART_9("'Лист2'!F10:F10"),
//    FIRST_VALUE_TEST2("'Лист1'!B2:B2"),
//    SECOND_VALUE_TEST2("'Лист1'!C5:C5"),
//    THIRD_VALUE_TEST2("'Лист1'!D7:D7"),
//    FOURTH_VALUE_TEST2("'Лист2'!C5:C5"),
//    FIFTH_VALUE_TEST2("'Лист2'!D7:D7");

    // Diploma-test2
    ACTUAL_INCOME_1("'Лист2'!G2:G2"),
    ACTUAL_INCOME_2("'Лист2'!G3:G3"),
    ACTUAL_INCOME_3("'Лист2'!G4:G4"),
    ACTUAL_INCOME_4("'Лист2'!G5:G5"),
    ACTUAL_INCOME_5("'Лист2'!G6:G6"),
    ACTUAL_INCOME_6("'Лист2'!G7:G7"),
    ACTUAL_INCOME_7("'Лист2'!G8:G8"),
    ACTUAL_INCOME_8("'Лист2'!G9:G9"),
    ACTUAL_INCOME_9("'Лист2'!G10:G10"),

//    Diploma-test
    LOCATION_PLAN_1("'Лист1'!F2:F2"),
    LOCATION_PLAN_2("'Лист1'!F3:F3"),
    LOCATION_PLAN_3("'Лист1'!F4:F4"),
    LOCATION_PLAN_4("'Лист1'!F5:F5"),
    LOCATION_PLAN_5("'Лист1'!F6:F6"),
    LOCATION_PLAN_6("'Лист1'!F7:F7"),
    LOCATION_PLAN_7("'Лист1'!F8:F8"),
    LOCATION_PLAN_8("'Лист1'!F9:F9"),
    LOCATION_PLAN_9("'Лист1'!F10:F10"),

    MAX_DAILY_REVENUE_1("'Лист1'!F2:F2"),
    MAX_DAILY_REVENUE_2("'Лист1'!F3:F3"),
    MAX_DAILY_REVENUE_3("'Лист1'!F4:F4"),
    MAX_DAILY_REVENUE_4("'Лист1'!F5:F5"),
    MAX_DAILY_REVENUE_5("'Лист1'!F6:F6"),
    MAX_DAILY_REVENUE_6("'Лист1'!F7:F7"),
    MAX_DAILY_REVENUE_7("'Лист1'!F8:F8"),
    MAX_DAILY_REVENUE_8("'Лист1'!F9:F9"),
    MAX_DAILY_REVENUE_9("'Лист1'!F10:F10"),

    PLAN_COMPLETION_PERCENT_1("'Лист1'!F2:F2"),
    PLAN_COMPLETION_PERCENT_2("'Лист1'!F3:F3"),
    PLAN_COMPLETION_PERCENT_3("'Лист1'!F4:F4"),
    PLAN_COMPLETION_PERCENT_4("'Лист1'!F5:F5"),
    PLAN_COMPLETION_PERCENT_5("'Лист1'!F6:F6"),
    PLAN_COMPLETION_PERCENT_6("'Лист1'!F7:F7"),
    PLAN_COMPLETION_PERCENT_7("'Лист1'!F8:F8"),
    PLAN_COMPLETION_PERCENT_8("'Лист1'!F9:F9"),
    PLAN_COMPLETION_PERCENT_9("'Лист1'!F10:F10");



    private final String propertyKey;

    SheetsRanges(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
