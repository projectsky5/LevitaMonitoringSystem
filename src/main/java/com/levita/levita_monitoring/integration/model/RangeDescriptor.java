package com.levita.levita_monitoring.integration.model;

import lombok.Getter;

@Getter
public record RangeDescriptor(String category, int id, String range) {
}
