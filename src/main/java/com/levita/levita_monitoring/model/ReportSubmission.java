package com.levita.levita_monitoring.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "report_submission")
public class ReportSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant submittedAt;

    private Instant rollBackAt;

    public ReportSubmission() {
    }

    public ReportSubmission(Long userId, Instant submittedAt) {
        this.userId = userId;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getRollBackAt() {
        return rollBackAt;
    }

    public void setRollBackAt(Instant rollBackAt) {
        this.rollBackAt = rollBackAt;
    }
}
