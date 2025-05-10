package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.dto.ReportStatusDto;
import com.levita.levita_monitoring.model.ReportSubmission;
import com.levita.levita_monitoring.repository.ReportSubmissionsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Optional;

@Service
public class ReportSubmissionService {

    private final ReportSubmissionsRepository repository;

    public ReportSubmissionService(ReportSubmissionsRepository reportSubmissionsRepository) {
        this.repository = reportSubmissionsRepository;
    }

    @Transactional
    public ReportSubmission submitReport(Long userId){
        ReportSubmission submission = new ReportSubmission(userId, Instant.now());
        return repository.save(submission);
    }

    @Transactional
    public ReportSubmission rollbackReport(Long userId) {
        ReportSubmission submission = repository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new IllegalStateException("No submission to rollback"));
        submission.setRollBackAt(Instant.now());
        return repository.save(submission);
    }

    public ReportStatusDto getStatus(Long userId) {
        Optional<ReportSubmission> optional = repository.findTopByUserIdOrderBySubmittedAtDesc(userId);
        Instant deadline = computeDailyDeadline();
        if (optional.isEmpty()) {
            return new ReportStatusDto(null, null, deadline);
        }
        ReportSubmission submission = optional.get();
        return new ReportStatusDto(submission.getSubmittedAt(), submission.getRollBackAt(), deadline);
    }

    private Instant computeDailyDeadline(){
        ZoneId msk = ZoneId.of("Europe/Moscow");
        LocalDate today = LocalDate.now(msk);
        LocalTime deadlineTime = LocalTime.of(2, 0);
        return ZonedDateTime.of(today, deadlineTime, msk).toInstant();
    }
}
