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
        Instant now = Instant.now();
        Optional<ReportSubmission> optReportSubmission = repository
                .findTopByUserIdOrderBySubmittedAtDesc(userId);

        ReportSubmission submission = optReportSubmission
                .map(existing -> {
                    existing.setSubmittedAt(now);
                    existing.setRollBackAt(null);
                    return existing;
                })
                .orElseGet(() -> {
                    ReportSubmission first = new ReportSubmission(userId, now);
                    first.setRollBackAt(null);
                    return first;
                });

        return repository.save(submission);
    }

    @Transactional
    public ReportSubmission rollbackReport(Long userId) {
        ReportSubmission submission = repository
                .findTopByUserIdOrderBySubmittedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("No submission to rollback"));

        Instant now = Instant.now();
        submission.setRollBackAt(now);
        submission.setSubmittedAt(null);

        return repository.save(submission);
    }

    public ReportStatusDto getStatus(Long userId) {
        ZoneId msk = ZoneId.of("Europe/Moscow");
        ZonedDateTime nowMsk = ZonedDateTime.now(msk);
        LocalDate today = nowMsk.toLocalDate();
        LocalTime time = nowMsk.toLocalTime();

        // 1) начало «рабочего» периода
        ZonedDateTime periodStartMsk;
        if (time.isBefore(LocalTime.of(2, 0))) {
            // до 02:00 — отчёт за «вчерашний» день, период начинался вчера в 09:00
            periodStartMsk = ZonedDateTime.of(
                    today.minusDays(1),
                    LocalTime.of(9, 0),
                    msk
            );
        } else {
            // после 02:00 — отчёт за «сегодняшний» день, период начался сегодня в 09:00
            periodStartMsk = ZonedDateTime.of(
                    today,
                    LocalTime.of(9, 0),
                    msk
            );
        }
        Instant periodStart = periodStartMsk.toInstant();

        // 2) пытаемся достать последнюю запись (по полю createdAt)
        Optional<ReportSubmission> opt = repository
                .findTopByUserIdOrderByCreatedAtDesc(userId);

        ReportSubmission todaySubmission;
        if (opt.isEmpty() || opt.get().getCreatedAt().isBefore(periodStart)) {
            // 3) нет «сегодняшней» — создаём чистую
            todaySubmission = new ReportSubmission();
            todaySubmission.setUserId(userId);
            todaySubmission.setCreatedAt(periodStart);
            todaySubmission.setSubmittedAt(null);
            todaySubmission.setRollBackAt(null);
            todaySubmission = repository.save(todaySubmission);
        } else {
            todaySubmission = opt.get();
        }

        // 4) собираем DTO
        Instant deadline = computeDailyDeadline();  // ваш метод
        return new ReportStatusDto(
                todaySubmission.getSubmittedAt(),
                todaySubmission.getRollBackAt(),
                deadline
        );
    }

    private Instant computeDailyDeadline(){
        ZoneId msk = ZoneId.of("Europe/Moscow");
        LocalDate today = LocalDate.now(msk);
        LocalTime deadlineTime = LocalTime.of(2, 0);
        return ZonedDateTime.of(today, deadlineTime, msk).toInstant();
    }
}
