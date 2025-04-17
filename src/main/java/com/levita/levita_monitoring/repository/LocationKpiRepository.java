package com.levita.levita_monitoring.repository;

import com.levita.levita_monitoring.model.LocationKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LocationKpiRepository extends JpaRepository<LocationKpi, Long> {
}
