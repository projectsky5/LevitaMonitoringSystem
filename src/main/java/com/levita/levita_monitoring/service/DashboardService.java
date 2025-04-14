package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final UserKpiRepository userKpiRepository;
    private final LocationKpiRepository locationKpiRepository;

    public DashboardService(UserRepository userRepository,
                            UserKpiRepository userKpiRepository,
                            LocationKpiRepository locationKpiRepository) {
        this.userRepository = userRepository;
        this.userKpiRepository = userKpiRepository;
        this.locationKpiRepository = locationKpiRepository;
    }

    @Transactional (readOnly = true)
    public DashboardDto getDashboardForUser(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserKpi userKpi = userKpiRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User KPI not found"));

        LocationKpi locationKpi = locationKpiRepository.findById(user.getLocation().getId())
                .orElseThrow(() -> new RuntimeException("Location KPI not found"));

        return DashboardDto.builder()
                .username(user.getName())
                .locationName(user.getLocation().getName())

                .conversionRate(userKpi.getConversionRate())
                .currentIncome(userKpi.getCurrentIncome())
                .plannedIncome(userKpi.getPlannedIncome())
                .mainSalaryPart(userKpi.getMainSalaryPart())
                .personalRevenue(userKpi.getPersonalRevenue())
                .dayBonuses(userKpi.getDayBonuses())

                .dailyFigure(locationKpi.getDailyFigure())
                .remainingToPlan(locationKpi.getLocationRemainingToPlan())
                .locationPlan(locationKpi.getLocationPlan())
                .maxDailyRevenue(locationKpi.getMaxDailyRevenue())
                .planCompletionPercent(locationKpi.getPlanCompletionPercent())
                .avgRevenuePerDay(locationKpi.getAvgRevenuePerDay())
                .actualIncome(locationKpi.getActualIncome())

                .build();
    }


}
