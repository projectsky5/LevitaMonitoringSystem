package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.service.SanitizationService;
import org.springframework.stereotype.Service;

@Service
public class LocationCreatorImpl implements LocationCreator {

    private final LocationRepository locationRepository;
    private final SanitizationService sanitizationService;

    public LocationCreatorImpl(LocationRepository locationRepository,
                               SanitizationService sanitizationService) {
        this.locationRepository = locationRepository;
        this.sanitizationService = sanitizationService;
    }

    @Override
    public boolean createIfNotExists(String rawLocation) {
        String name = sanitizationService.sanitize(rawLocation);
        if (name.isBlank()) {
            return false;
        }
        boolean exists = locationRepository.findAll().stream()
                .anyMatch(loc -> loc.getName().equals(name));
        if (exists) {
            return false;
        }
        Location location = new Location();
        location.setName(name);
        locationRepository.save(location);
        return true;
    }
}
