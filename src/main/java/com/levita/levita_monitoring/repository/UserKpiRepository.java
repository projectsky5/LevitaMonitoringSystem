package com.levita.levita_monitoring.repository;

import com.levita.levita_monitoring.model.UserKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserKpiRepository extends JpaRepository<UserKpi, Long> {
}
