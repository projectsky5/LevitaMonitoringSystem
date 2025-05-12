package com.levita.levita_monitoring.dto;

import jakarta.validation.constraints.NotBlank;

public record RollbackReportDto(
        @NotBlank String reportDate
) {
}
