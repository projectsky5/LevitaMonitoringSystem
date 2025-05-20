package com.levita.levita_monitoring.enums;

public enum Category {
    USERS("USERS"),
    LOCATIONS("LOCATIONS");

    private final String category;

    Category(String category) {
        this.category = category;
    }
    public String get() {
        return category;
    }
}
