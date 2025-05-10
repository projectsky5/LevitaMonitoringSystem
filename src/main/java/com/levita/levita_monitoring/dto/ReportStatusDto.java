package com.levita.levita_monitoring.dto;

import java.time.Instant;

public class ReportStatusDto {
    private final Instant submittedAt;
    private final Instant rolledBackAt;
    private final Instant rollbackDeadline;

    public ReportStatusDto(Instant submittedAt, Instant rolledBackAt, Instant rollbackDeadline) {
        this.submittedAt = submittedAt;
        this.rolledBackAt = rolledBackAt;
        this.rollbackDeadline = rollbackDeadline;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getRolledBackAt() {
        return rolledBackAt;
    }

    public Instant getRollbackDeadline() {
        return rollbackDeadline;
    }
}
