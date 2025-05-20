package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.enums.Category;
import com.levita.levita_monitoring.enums.Kpi;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.service.parser.LocationCreator;
import com.levita.levita_monitoring.service.parser.UserCreator;
import com.levita.levita_monitoring.service.parser.LocationKpiUpdater;
import com.levita.levita_monitoring.service.parser.UserKpiUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KpiDataService {

    private static final Logger log = LoggerFactory.getLogger(KpiDataService.class);

    private final UserCreator userCreator;
    private final LocationCreator locationCreator;
    private final UserKpiUpdater userKpiUpdater;
    private final LocationKpiUpdater locationKpiUpdater;

    public KpiDataService(UserCreator userCreator,
                          LocationCreator locationCreator,
                          UserKpiUpdater userKpiUpdater,
                          LocationKpiUpdater locationKpiUpdater) {
        this.userCreator = userCreator;
        this.locationCreator = locationCreator;
        this.userKpiUpdater = userKpiUpdater;
        this.locationKpiUpdater = locationKpiUpdater;
    }

    @Transactional
    public void saveDataFromSheets(RangeDescriptor rangeDescriptor, String value) {
        String category = rangeDescriptor.category();

        if (Category.USERS.get().equals(category)) {
            userCreator.createIfNotExists(value);
            return;
        }

        if (Category.LOCATIONS.get().equals(category)) {
            locationCreator.createIfNotExists(value);
            return;
        }

        Kpi kpi = Kpi.fromTopic(category);
        if (kpi == null) {
            throw new IllegalArgumentException("Unsupported category: " + category);
        }

        if (kpi.isLocationCategory()) {
            locationKpiUpdater.update(category, rangeDescriptor.label(), value);
        } else {
            userKpiUpdater.update(category, rangeDescriptor.label(), value);
        }
    }
}