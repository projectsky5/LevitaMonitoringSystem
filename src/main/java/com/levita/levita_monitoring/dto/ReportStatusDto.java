package com.levita.levita_monitoring.dto;

import java.time.Instant;

public class ReportStatusDto {
    private final Instant submittedAt;
    private final Instant rollBackAt;
    private final Instant rollbackDeadline;

    public ReportStatusDto(Instant submittedAt, Instant rollBackAt, Instant rollbackDeadline) {
        this.submittedAt = submittedAt;
        this.rollBackAt = rollBackAt;
        this.rollbackDeadline = rollbackDeadline;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getRolledBackAt() {
        return rollBackAt;
    }

    public Instant getRollbackDeadline() {
        return rollbackDeadline;
    }
}
