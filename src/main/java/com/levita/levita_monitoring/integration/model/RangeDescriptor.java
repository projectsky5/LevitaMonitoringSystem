package com.levita.levita_monitoring.integration.model;

public record RangeDescriptor(String category, int id, String range) {

    public String getCategory() {
        return category;
    }


    public int getId() {
        return id;
    }


    public String getRange() {
        return range;
    }
}
