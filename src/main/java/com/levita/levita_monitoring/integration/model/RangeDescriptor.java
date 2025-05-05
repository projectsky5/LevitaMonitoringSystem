package com.levita.levita_monitoring.integration.model;

public record RangeDescriptor(int id, String range, String category, String label) {

    public String getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public String getRange() {
        return range;
    }

    public String getLabel() {
        return label;
    }
}

