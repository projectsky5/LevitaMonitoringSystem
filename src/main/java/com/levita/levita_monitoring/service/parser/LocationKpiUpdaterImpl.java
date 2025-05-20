package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.enums.Kpi;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.service.SanitizationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class LocationKpiUpdaterImpl implements LocationKpiUpdater {
    private final LocationRepository locationRepository;
    private final LocationKpiRepository kpiRepository;
    private final SanitizationService sanitization;

    private final Map<String, BiConsumer<LocationKpi, String>> setters = new HashMap<>();

    public LocationKpiUpdaterImpl(LocationRepository locationRepository,
                                  LocationKpiRepository kpiRepository,
                                  SanitizationService sanitization) {
        this.locationRepository = locationRepository;
        this.kpiRepository = kpiRepository;
        this.sanitization = sanitization;

        setters.put(Kpi.LOCATION_INCOME.getTopic(), (kpi, raw) ->
                kpi.setActualIncome(new BigDecimal(sanitization.sanitize(raw)))
        );
        setters.put(Kpi.PLAN.getTopic(), (kpi, raw) ->
                kpi.setLocationPlan(new BigDecimal(sanitization.sanitize(raw)))
        );
        setters.put(Kpi.MAX_REVENUE.getTopic(), (kpi, raw) ->
                kpi.setMaxDailyRevenue(new BigDecimal(sanitization.sanitize(raw)))
        );
        setters.put(Kpi.PLAN_COMPLETION.getTopic(), (kpi, raw) ->
                kpi.setPlanCompletionPercent(Double.parseDouble(sanitization.sanitize(raw)))
        );
        setters.put(Kpi.REMAINING.getTopic(), (kpi, raw) ->
                kpi.setLocationRemainingToPlan(new BigDecimal(sanitization.sanitize(raw)))
        );
        setters.put(Kpi.DAILY_FIGURE.getTopic(), (kpi, raw) ->
                kpi.setDailyFigure(new BigDecimal(sanitization.sanitize(raw)))
        );
        setters.put(Kpi.AVG_REVENUE_PER_DAY.getTopic(), (kpi, raw) ->
                kpi.setAvgRevenuePerDay(new BigDecimal(sanitization.sanitize(raw)))
        );
    }

    @Override
    public void update(String topic, String rawLocation, String rawValue) {
        BiConsumer<LocationKpi, String> setter = setters.get(topic);
        if (setter == null) {
            throw new IllegalArgumentException("Unknown location KPI topic: " + topic);
        }

        // найти локацию
        var loc = locationRepository.findAll().stream()
                .filter(l -> l.getName().equalsIgnoreCase(rawLocation))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + rawLocation));

        // получить или создать KPI
        LocationKpi kpi = kpiRepository.findById(loc.getId())
                .orElseGet(() -> {
                    var newK = new LocationKpi();
                    newK.setLocation(loc);
                    return kpiRepository.save(newK);
                });

        // применить и сохранить
        try {
            setter.accept(kpi, rawValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid KPI value: " + rawValue, ex);
        }
        kpiRepository.save(kpi);
    }
}
