package com.levita.levita_monitoring.repository;

import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.LocationKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationKpiRepository extends JpaRepository<LocationKpi, Long> {
}
