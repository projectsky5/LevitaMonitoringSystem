package com.levita.levita_monitoring.repository;

import com.levita.levita_monitoring.model.ReportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportSubmissionsRepository extends JpaRepository<ReportSubmission, Long> {
    Optional<ReportSubmission> findTopByUserIdOrderBySubmittedAtDesc(Long userId);
}
