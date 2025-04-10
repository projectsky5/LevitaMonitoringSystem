package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class KpiDataService {

    private final UserKpiRepository userKpiRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationKpiRepository locationKpiRepository;

    @Autowired
    public KpiDataService(UserKpiRepository userKpiRepository,
                          UserRepository userRepository,
                          LocationRepository locationRepository,
                          LocationKpiRepository locationKpiRepository) {
        this.userKpiRepository = userKpiRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationKpiRepository = locationKpiRepository;
    }

    public void saveDataFromSheets(RangeDescriptor rangeDescriptor, String value) {
        String category = rangeDescriptor.category();
        int id = rangeDescriptor.id();

        switch (category) {
            case "CONVERSION_RATE":
                handleConversionRate(id, value);
                break;
            case "MAIN_SALARY_PART":
                handleMainSalaryPart(id, value);
                break;
            case "ACTUAL_INCOME":
                handleActualIncome(id, value);
                break;
            case "REMAINING_TO_PLAN":
                handleRemainingToPlan(id, value);
                break;
            case "LOCATION_PLAN":
                handleLocationPlan(id, value);
                break;
            case "MAX_DAILY_REVENUE":
                handleMaxDailyRevenue(id, value);
                break;
            case "PLAN_COMPLETION_PERCENT":
                handlePlanCompletionPercent(id, value);
                break;
            case "PERSONAL_REVENUE":
                handlePersonalRevenue(id, value);
                break;
            default:
                System.out.printf("Нет обработки для категории: %s\n", category);
                break;
            // TODO: Остальные показатели
        }
    }

    private void handleRemainingToPlan(int locationId, String value) {
        try {
            BigDecimal remainingToPlan = new BigDecimal(sanitizeNumericString(value));

            Optional<LocationKpi> optKpi = getLocationKpi(locationId);
            if (optKpi.isEmpty()) {
                return;
            }
            LocationKpi locationKpi = optKpi.get();
            locationKpi.setLocationRemainingToPlan(remainingToPlan);
            locationKpiRepository.save(locationKpi);
            System.out.printf("Сохранен remainingToPlan для локации с id %d: %s\n", locationId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для REMAINING_TO_PLAN_%d: %s\n", locationId, value);
        }
    }

    private void handleLocationPlan(int locationId, String value) {
        try {
            BigDecimal locationPlan = new BigDecimal(sanitizeNumericString(value));

            Optional<LocationKpi> optKpi = getLocationKpi(locationId);
            if (optKpi.isEmpty()) {
                return;
            }
            LocationKpi locationKpi = optKpi.get();
            locationKpi.setLocationPlan(locationPlan);
            locationKpiRepository.save(locationKpi);
            System.out.printf("Сохранен locationPlan для локации с id %d: %s\n", locationId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для LOCATION_PLAN_%d: %s\n", locationId, value);
        }
    }

    private void handleMaxDailyRevenue(int locationId, String value) {
        try {
            BigDecimal maxDailyRevenue = new BigDecimal(sanitizeNumericString(value));

            Optional<LocationKpi> optKpi = getLocationKpi(locationId);
            if (optKpi.isEmpty()) {
                return;
            }
            LocationKpi locationKpi = optKpi.get();
            locationKpi.setMaxDailyRevenue(maxDailyRevenue);
            locationKpiRepository.save(locationKpi);
            System.out.printf("Сохранен maxDailyRevenue для локации с id %d: %s\n", locationId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для MAX_DAILY_REVENUE_%d: %s\n", locationId, value);
        }
    }

    private void handlePlanCompletionPercent(int locationId, String value) {
        try {
            Double planCompletionPercent = Double.valueOf(sanitizeNumericString(value));

            Optional<LocationKpi> optKpi = getLocationKpi(locationId);
            if (optKpi.isEmpty()) {
                return;
            }
            LocationKpi locationKpi = optKpi.get();
            locationKpi.setPlanCompletionPercent(planCompletionPercent);
            locationKpiRepository.save(locationKpi);
            System.out.printf("Сохранен planCompletionPercent для локации с id %d: %s\n", locationId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для PLAN_COMPLETION_PERCENT_%d: %s\n", locationId, value);
        }
    }

    private void handleActualIncome(int locationId, String value) {
        try {
            BigDecimal actualIncome = new BigDecimal(sanitizeNumericString(value));

            Optional<LocationKpi> optKpi = getLocationKpi(locationId);
            if (optKpi.isEmpty()) {
                return;
            }
            LocationKpi locationKpi = optKpi.get();
            locationKpi.setActualIncome(actualIncome);
            locationKpiRepository.save(locationKpi);
            System.out.printf("Сохранен actualIncome для локации с id %d: %s\n", locationId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для ACTUAL_INCOME_%d: %s\n", locationId, value);
        }
    }

    private void handleConversionRate(int userId, String value) {
        try {
            Double conversion = Double.valueOf(sanitizeNumericString(value));

            Optional<UserKpi> optKpi = getUserKpi(userId);
            if (optKpi.isEmpty()) {
                return;
            }
            UserKpi userKpi = optKpi.get();
            userKpi.setConversionRate(conversion);
            userKpiRepository.save(userKpi);

            System.out.printf("Сохранена conversionRate для пользователя с индексом %d: %.1f\n", userId, conversion);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для CONVERSATION_RATE_%d: %s\n", userId, value);
        }
    }

    private void handleMainSalaryPart(int userId, String value) {
        try {
            BigDecimal mainSalaryPart = new BigDecimal(sanitizeNumericString(value));

            Optional<UserKpi> optKpi = getUserKpi(userId);
            if (optKpi.isEmpty()) {
                return;
            }
            UserKpi userKpi = optKpi.get();
            userKpi.setMainSalaryPart(mainSalaryPart);
            userKpiRepository.save(userKpi);
            System.out.printf("Сохранен mainSalaryPart для пользователя с id %d: %s\n", userId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для MAIN_SALARY_PART_%d: %s\n", userId, value);
        }
    }

    private void handlePersonalRevenue(int userId, String value) {
        try {
            BigDecimal personalRevenue = new BigDecimal(sanitizeNumericString(value));

            Optional<UserKpi> optKpi = getUserKpi(userId);
            if (optKpi.isEmpty()) {
                return;
            }
            UserKpi userKpi = optKpi.get();
            userKpi.setPersonalRevenue(personalRevenue);
            userKpiRepository.save(userKpi);
            System.out.printf("Сохранена personalRevenue для пользователя с id %d: %s\n", userId, value);
        } catch (NumberFormatException e) {
            System.err.printf("Неверный формат для PERSONAL_REVENUE_%d: %s\n", userId, value);
        }
    }

    private Optional<LocationKpi> getLocationKpi(int locationId) {
        Optional<Location> optLocation = locationRepository.findById((long) locationId);
        if (optLocation.isEmpty()) {
            System.out.printf("Локация с id %d не найдена\n", locationId);
            return Optional.empty();
        }
        Location location = optLocation.get();
        Optional<LocationKpi> optKpi = locationKpiRepository.findByLocation(location);
        LocationKpi locationKpi = optKpi.orElseGet(() -> {
            LocationKpi newKpi = new LocationKpi();
            newKpi.setLocation(location);
            return newKpi;
        });
        return Optional.of(locationKpi);
    }

    private Optional<UserKpi> getUserKpi(int userId) {
        Optional<User> optUser = userRepository.findById((long) userId);
        if (optUser.isEmpty()) {
            System.out.printf("Пользователь с id %d не найден\n", userId);
            return Optional.empty();
        }
        User user = optUser.get();
        Optional<UserKpi> optKpi = userKpiRepository.findByUser(user);
        UserKpi userKpi = optKpi.orElseGet(() -> {
            UserKpi newKpi = new UserKpi();
            newKpi.setUser(user);
            return newKpi;
        });
        return Optional.of(userKpi);
    }

    private String sanitizeNumericString(String value) {
        return value.replace("\u00A0", "")
                .replace(" ", "")
                .replace(",", ".")
                .replace("%", "")
                .replace("₽", "")
                .trim();
    }
}
