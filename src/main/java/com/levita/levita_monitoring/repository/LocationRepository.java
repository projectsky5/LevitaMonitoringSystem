package com.levita.levita_monitoring.repository;

import com.levita.levita_monitoring.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
