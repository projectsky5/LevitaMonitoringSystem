package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.dto.AdminDto;
import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final UserKpiRepository userKpiRepository;
    private final LocationKpiRepository locationKpiRepository;

    private static final Map<String, BiFunction<String, String, Comparator<User>>> COMPARATOR_BUILDERS = Map.of(
            "conversionRate", (sortField, order) -> buildComparator(user -> user.getUserKpi().getConversionRate(), order, Double::compareTo),
            "personalRevenue", (sortField, order) -> buildComparator(user -> user.getUserKpi().getPersonalRevenue(), order, BigDecimal::compareTo)
    );

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

        return buildDashboardFor(user);
    }

    @Transactional (readOnly = true)
    public DashboardDto getDashboardByUserId(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildDashboardFor(user);
    }

    private DashboardDto buildDashboardFor(User user) {
        UserKpi userKpi = userKpiRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User KPI not found"));

        LocationKpi locationKpi = locationKpiRepository.findById(user.getLocation().getId())
                .orElseThrow(() -> new RuntimeException("Location KPI not found"));

        return DashboardDto.builder()
                .username(user.getName())
                .locationName(user.getLocation().getName())

                .conversionRate(userKpi.getConversionRate())
                .currentIncome(userKpi.getCurrentIncome())
//                .plannedIncome(userKpi.getPlannedIncome())
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

    @Transactional (readOnly = true)
    public List<AdminDto> getAllAdmins() {
        return userRepository.findAllByRole(Role.ADMIN).stream()
                .map(AdminDto::new)
                .toList();
    }

    @Transactional (readOnly = true)
    public List<AdminDto> getAllAdminsSorted(String primarySort, String primaryOrder, String secondarySort, String secondaryOrder) {
        List<User> admins = userRepository.findAllAdminsWithKpi(Role.ADMIN);

        Comparator<User> comparator = Optional.ofNullable(COMPARATOR_BUILDERS.get(primarySort))
                .map(builder -> builder.apply(primarySort, primaryOrder))
                .orElse(null);

        Comparator<User> secondaryComparator = Optional.ofNullable(COMPARATOR_BUILDERS.get(secondarySort))
                .map(builder -> builder.apply(secondarySort, secondaryOrder))
                .orElse(null);

        if (comparator != null && secondaryComparator != null && !primarySort.equals(secondarySort)) {
            comparator = comparator.thenComparing(secondaryComparator);
        }

        if(comparator != null){
            admins.sort(comparator);
        }

        return admins.stream()
                .map(AdminDto::new)
                .toList();
    }

    private static <T> Comparator<User> buildComparator(Function<User, T> keyExtractor, String order, Comparator<T> valueComparator) {
        Comparator<User> comparator = Comparator.comparing(keyExtractor, Comparator.nullsLast(valueComparator));
        return "desс".equalsIgnoreCase(order) ? comparator.reversed() : comparator;
    }
}
