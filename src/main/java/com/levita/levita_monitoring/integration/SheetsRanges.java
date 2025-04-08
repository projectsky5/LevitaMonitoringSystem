package com.levita.levita_monitoring.integration;

public enum SheetsRanges {

    // Для diploma-test

    FIRST_VALUE_TEST("'Лист1'!B2:B2"),
    SECOND_VALUE_TEST("'Лист1'!C5:C5"),
    THIRD_VALUE_TEST("'Лист1'!D7:D7"),
    FOURTH_VALUE_TEST("'Лист2'!C5:C5"),
    FIFTH_VALUE_TEST("'Лист2'!D7:D7"),

    // Для diploma-test2

    FIRST_VALUE_TEST2("'Лист1'!B2:B2"),
    SECOND_VALUE_TEST2("'Лист1'!C5:C5"),
    THIRD_VALUE_TEST2("'Лист1'!D7:D7"),
    FOURTH_VALUE_TEST2("'Лист2'!C5:C5"),
    FIFTH_VALUE_TEST2("'Лист2'!D7:D7");

    private final String propertyKey;

    SheetsRanges(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
