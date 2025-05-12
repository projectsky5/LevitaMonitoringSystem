package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import com.levita.levita_monitoring.security.CredentialsGenerator;
import com.levita.levita_monitoring.util.SanitizationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class KpiDataService {

    private static final Logger log = LoggerFactory.getLogger(KpiDataService.class);

    private final UserKpiRepository userKpiRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationKpiRepository locationKpiRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public KpiDataService(UserKpiRepository userKpiRepository,
                          UserRepository userRepository,
                          LocationRepository locationRepository,
                          LocationKpiRepository locationKpiRepository,
                          PasswordEncoder passwordEncoder) {
        this.userKpiRepository = userKpiRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationKpiRepository = locationKpiRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void saveDataFromSheets(RangeDescriptor rangeDescriptor, String value) {
        String category = rangeDescriptor.category();

        if(category.equals("USERS")){
            handleUserCreation(value);
            return;
        }

        if(category.equals("LOCATIONS")){
            handleLocationCreation(value);
            return;
        }

        if(isUserKpiCategory(category)){
            String rawUser = rangeDescriptor.label();
            handleUserKpiByNameAndLocation(category, rawUser, value);
            return;
        }

        if(isLocationKpiCategory(category)){
            String rawLocation = rangeDescriptor.label();
            handleLocationKpiByName(category, rawLocation, value);
            return;
        }

        log.warn("Нет обработки для категории: {}", category);

    }

    private void handleUserCreation(String rawUser) {
        String[] nameAndLocation = extractNameAndLocation(rawUser);
        String name = nameAndLocation[0];
        String locationName = nameAndLocation[1];

        if(name.isBlank() || locationName.isBlank()){
            log.warn("Некорректное имя или локация: {}", rawUser);
            return;
        }

        Location location = locationRepository
                .findByNameIgnoreCase(locationName)
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName(locationName);
                    return locationRepository.save(loc);
                });

        boolean exists = userRepository
                .existsByNameIgnoreCaseAndLocation_NameIgnoreCase(name, locationName);

        if(!exists){
            createUser(name, location, locationName);
        }
    }

    private void createUser(String name, Location location, String locationName){
        Map<String, String> credentials = CredentialsGenerator.generateCredentials(name, locationName);
        String login = credentials.get("login");
        String rawPassword = credentials.get("password");

        User user = new User();
        user.setName(name);
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setLocation(location);
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        log.info("Создан пользователь [{} ({})]", name, locationName);


    }

    private void handleLocationCreation(String value) {
        String sanitizedName = SanitizationUtils.sanitizeNumeric(value);
        if(sanitizedName.isBlank()){
            return;
        }

        boolean exists = locationRepository.existsByNameIgnoreCase(sanitizedName);

        if(!exists){
            Location location = new Location();
            location.setName(sanitizedName);
            locationRepository.save(location);
            log.info("Создана локация [{}]", sanitizedName);
        }
    }

    private boolean isUserKpiCategory(String category) {
        return switch(category){
            case "CONVERSION_RATE", "MAIN_SALARY_PART", "PERSONAL_REVENUE", "CURRENT_INCOME", "DAY_BONUSES" -> true;
            default -> false;
        };
    }

    private boolean isLocationKpiCategory(String category) {
        return switch(category){
            case "ACTUAL_INCOME", "LOCATION_PLAN", "MAX_DAILY_REVENUE", "PLAN_COMPLETION_PERCENT", "REMAINING_TO_PLAN", "DAILY_FIGURE", "AVG_REVENUE_PER_DAY" -> true;
            default -> false;
        };
    }

    private void handleUserKpiByNameAndLocation(String category, String rawUser, String value){
        Optional<UserKpi> optKpi = getUserKpiByNameAndLocation(rawUser);
        if(optKpi.isEmpty()) {
            log.warn("Пользователь не найден для [{}]: {}", category, rawUser);
            return;
        }

        UserKpi userKpi = optKpi.get();

        try{
            switch(category){
                case "CONVERSION_RATE" -> userKpi.setConversionRate(Double.valueOf(SanitizationUtils.sanitizeNumeric(value)));
                case "MAIN_SALARY_PART" -> userKpi.setMainSalaryPart(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "PERSONAL_REVENUE" -> userKpi.setPersonalRevenue(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "CURRENT_INCOME" -> userKpi.setCurrentIncome(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "DAY_BONUSES" -> userKpi.setDayBonuses(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
            }
            userKpiRepository.save(userKpi);
            log.info("Сохранено [{}] для пользователя [{}]: {}", category, rawUser, value);
        } catch (NumberFormatException e){
            log.error("Ошибка парсинга [{}] для [{}]: {}", category, rawUser, value);
        }
    }

    private Optional<UserKpi> getUserKpiByNameAndLocation(String rawUser){
        String[] nameAndLocation = extractNameAndLocation(rawUser);
        String name = nameAndLocation[0];
        String location = nameAndLocation[1];

        return userRepository
                .findByNameIgnoreCaseAndLocation_NameIgnoreCase(name, location)
                .map(user -> userKpiRepository.findById(user.getId())
                        .orElseGet(() -> {
                            UserKpi newKpi = new UserKpi();
                            newKpi.setUser(user);
                            return userKpiRepository.save(newKpi);
                        }));
    }

    private void handleLocationKpiByName(String category, String rawLocation, String value){
        if(rawLocation == null || rawLocation.isBlank()){
            log.warn("Ошибка: отсутствует label (название локации) для [{}]",category);
            return;
        }

        Optional<LocationKpi> optKpi = getLocationKpiByName(rawLocation.trim());

        if(optKpi.isEmpty()) {
            log.warn("Локация не найдена: {}", rawLocation);
            return;
        }

        LocationKpi locationKpi = optKpi.get();

        try{
            switch(category){
                case "ACTUAL_INCOME" -> locationKpi.setActualIncome(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "LOCATION_PLAN" -> locationKpi.setLocationPlan(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "MAX_DAILY_REVENUE" -> locationKpi.setMaxDailyRevenue(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "PLAN_COMPLETION_PERCENT" -> locationKpi.setPlanCompletionPercent(Double.valueOf(SanitizationUtils.sanitizeNumeric(value)));
                case "REMAINING_TO_PLAN" -> locationKpi.setLocationRemainingToPlan(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "DAILY_FIGURE" -> locationKpi.setDailyFigure(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
                case "AVG_REVENUE_PER_DAY" -> locationKpi.setAvgRevenuePerDay(new BigDecimal(SanitizationUtils.sanitizeNumeric(value)));
            }
            locationKpiRepository.save(locationKpi);
            log.info("Сохранено [{}] для локации [{}]: {}", category, rawLocation, value);
        } catch (NumberFormatException e){
            log.error("Ошибка парсинга [{}] для локации [{}]: {}", category, rawLocation, value);
        }
    }

    private Optional<LocationKpi> getLocationKpiByName(String locationName){
        return locationRepository.findByNameIgnoreCase(locationName)
                .map(location -> locationKpiRepository.findById(location.getId())
                        .orElseGet(() -> {
                            LocationKpi newKpi = new LocationKpi();
                            newKpi.setLocation(location);
                            return locationKpiRepository.save(newKpi);
                        }));
    }

    private String[] extractNameAndLocation(String raw){
        String cleaned = raw.trim();
        String[] parts = cleaned.split("\\s*\\(|\\)");

        if(parts.length == 2){
            return new String[]{parts[0].trim(), parts[1].trim()};
        } else {
            return new String[]{cleaned, ""};
        }
    }
}
