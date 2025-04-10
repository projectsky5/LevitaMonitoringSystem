package com.levita.levita_monitoring.integration.enums;

public enum SheetsId {

    FIRST_TABLE("16c-SSNDJSQWj2jCHVWSoVOCnZMBbAHKAC3Tq5JOCi5U"), // Управляющие

    SECOND_TABLE("1BwQdns8KkBsZ0C4k8dC6BtGxaLNocbmDR0bfuGKXSJs"), // Показатели Администраторов

    THIRD_TABLE("1QcOiHd5pY3wxMtGvkeGMd7z826RQotHBWZ6u_mWWFg0"), // ЗП

    FOURTH_TABLE("1rFLR_5Zb43AVQedKCKcFXyLmO42Qt1CFMNHAqiFW8FU"); // Показатели на день

    private String propertyKey;

    SheetsId(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

}
