package com.levita.levita_monitoring.integration.enums;

public enum SheetsRanges {

    // Для diploma-test

    CONVERSION_RATE_1("'Лист1'!E2:E2"),
    CONVERSION_RATE_2("'Лист1'!E3:E3"),
//    FIRST_VALUE_TEST("'Лист1'!B2:B2"),
//    SECOND_VALUE_TEST("'Лист1'!C5:C5"),
//    THIRD_VALUE_TEST("'Лист1'!D7:D7"),
//    FOURTH_VALUE_TEST("'Лист2'!C5:C5"),
//    FIFTH_VALUE_TEST("'Лист2'!D7:D7"),

    // Для diploma-test2

    MAIN_SALARY_PART_1("'Лист2'!F2:F2"),
    MAIN_SALARY_PART_2("'Лист2'!F3:F3");
//    FIRST_VALUE_TEST2("'Лист1'!B2:B2"),
//    SECOND_VALUE_TEST2("'Лист1'!C5:C5"),
//    THIRD_VALUE_TEST2("'Лист1'!D7:D7"),
//    FOURTH_VALUE_TEST2("'Лист2'!C5:C5"),
//    FIFTH_VALUE_TEST2("'Лист2'!D7:D7");

    private final String propertyKey;

    SheetsRanges(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
