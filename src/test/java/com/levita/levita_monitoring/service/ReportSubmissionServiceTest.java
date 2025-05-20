package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.dto.ReportStatusDto;
import com.levita.levita_monitoring.model.ReportSubmission;
import com.levita.levita_monitoring.repository.ReportSubmissionsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportSubmissionServiceTest {

    @Mock
    private ReportSubmissionsRepository repository;

    @InjectMocks
    private ReportSubmissionService service;

    @Test
    void submitReport_whenNoExisting_createsNew() {
        Long userId = 1L;
        when(repository.findTopByUserIdOrderBySubmittedAtDesc(userId))
                .thenReturn(Optional.empty());
        when(repository.save(any(ReportSubmission.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReportSubmission result = service.submitReport(userId);

        assertEquals(userId, result.getUserId());
        assertNotNull(result.getSubmittedAt());
        assertNull(result.getRollBackAt());
        verify(repository).save(any(ReportSubmission.class));
    }

    @Test
    void submitReport_whenExisting_updatesTimestamp() {
        Long userId = 2L;
        Instant old = Instant.parse("2025-01-01T00:00:00Z");
        ReportSubmission existing = new ReportSubmission(userId, old);
        existing.setRollBackAt(Instant.parse("2025-01-02T00:00:00Z"));
        when(repository.findTopByUserIdOrderBySubmittedAtDesc(userId))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ReportSubmission result = service.submitReport(userId);

        assertSame(existing, result);
        assertNotEquals(old, result.getSubmittedAt());
        assertNull(result.getRollBackAt());
        verify(repository).save(existing);
    }

    @Test
    void rollbackReport_whenNoExisting_throws() {
        Long userId = 3L;
        when(repository.findTopByUserIdOrderBySubmittedAtDesc(userId))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.rollbackReport(userId));
    }

    @Test
    void rollbackReport_updatesFields() {
        Long userId = 4L;
        Instant submitTime = Instant.parse("2025-01-01T00:00:00Z");
        ReportSubmission existing = new ReportSubmission(userId, submitTime);
        when(repository.findTopByUserIdOrderBySubmittedAtDesc(userId))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ReportSubmission result = service.rollbackReport(userId);

        assertNull(result.getSubmittedAt());
        assertNotNull(result.getRollBackAt());
        verify(repository).save(existing);
    }

    @Test
    void getStatus_withNoExisting_createsAndReturnsDto() {
        Long userId = 5L;
        when(repository.findTopByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Optional.empty());
        when(repository.save(any(ReportSubmission.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReportStatusDto dto = service.getStatus(userId);

        assertNull(dto.getSubmittedAt());
        assertNull(dto.getRolledBackAt());
        Instant expectedDeadline = computeDailyDeadline();
        assertEquals(expectedDeadline, dto.getRollbackDeadline());
        verify(repository).save(any(ReportSubmission.class));
    }

    @Test
    void getStatus_existingWithinPeriod_returnsExisting() {
        Long userId = 6L;
        Instant now = Instant.now();
        ReportSubmission existing = new ReportSubmission();
        existing.setUserId(userId);
        existing.setCreatedAt(now);
        Instant sub = Instant.parse("2025-01-01T01:00:00Z");
        Instant rb = Instant.parse("2025-01-01T02:00:00Z");
        existing.setSubmittedAt(sub);
        existing.setRollBackAt(rb);

        when(repository.findTopByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Optional.of(existing));

        ReportStatusDto dto = service.getStatus(userId);

        assertEquals(sub, dto.getSubmittedAt());
        assertEquals(rb, dto.getRolledBackAt());
        verify(repository, never()).save(any());
    }

    private Instant computeDailyDeadline() {
        ZoneId msk = ZoneId.of("Europe/Moscow");
        LocalDate today = LocalDate.now(msk);
        LocalTime deadlineTime = LocalTime.of(2, 0);
        return ZonedDateTime.of(today, deadlineTime, msk).toInstant();
    }
}