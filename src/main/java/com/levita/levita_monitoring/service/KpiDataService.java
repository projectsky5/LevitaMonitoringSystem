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
import org.springframework.transaction.annotation.Transactional;

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

        System.out.printf("Нет обработки для категории: %s\n", category);

    }

    private void handleUserCreation(String rawUser) {
        String[] nameAndLocation = extractNameAndLocation(rawUser);
        String name = nameAndLocation[0];
        String locationName = nameAndLocation[1];

        if(name.isBlank() || locationName.isBlank()){
            System.err.printf("Некорректное имя или локация: %s\n", rawUser);
            return;
        }

        Location location = locationRepository.findAll().stream()
                .filter( loc -> loc.getName().equalsIgnoreCase(locationName))
                .findFirst()
                .orElseGet( () -> {
                    Location loc = new Location();
                    loc.setName(locationName);
                    return locationRepository.save(loc);
                });

        boolean exists = userRepository.findAll().stream()
                .anyMatch( user -> user.getName().equalsIgnoreCase(name)
                        && user.getLocation() != null
                        && user.getLocation().getName().equalsIgnoreCase(locationName));

        if(!exists){
            User user = new User();
            user.setName(name);
            user.setLogin("user_" + name.toLowerCase() + "_" + locationName.toLowerCase());
            user.setPassword(String.valueOf(Math.random() * 100));
            user.setLocation(location);
            userRepository.save(user);
            System.out.printf("Создан пользователь [%s (%s)]\n", name, locationName);
        }
    }

    private void handleLocationCreation(String value) {
        String sanitizedName = sanitizeNumericString(value);
        if(sanitizedName.isBlank()){
            return;
        }

        boolean exists = locationRepository.findAll().stream()
                .anyMatch(location -> location.getName().equals(sanitizedName));

        if(!exists){
            Location location = new Location();
            location.setName(sanitizedName);
            locationRepository.save(location);
            System.out.printf("Создана локация [%s]\n", sanitizedName);
        }
    }

    private boolean isUserKpiCategory(String category) {
        return switch(category){
            case "CONVERSION_RATE", "MAIN_SALARY_PART", "PERSONAL_REVENUE" -> true;
            default -> false;
        };
    }

    private boolean isLocationKpiCategory(String category) {
        return switch(category){
            case "ACTUAL_INCOME", "REMAINING_TO_PLAN", "LOCATION_PLAN", "MAX_DAILY_REVENUE", "PLAN_COMPLETION_PERCENT" -> true;
            default -> false;
        };
    }

    private void handleUserKpiByNameAndLocation(String category, String rawUser, String value){
        Optional<UserKpi> optKpi = getUserKpiByNameAndLocation(rawUser);
        if(optKpi.isEmpty()) {
            System.err.printf("Пользователь не найден для [%s]: %s\n", category, rawUser);
            return;
        }

        UserKpi userKpi = optKpi.get();

        try{
            switch(category){
                case "CONVERSION_RATE" -> userKpi.setConversionRate(Double.valueOf(sanitizeNumericString(value)));
                case "MAIN_SALARY_PART" -> userKpi.setMainSalaryPart(new BigDecimal(sanitizeNumericString(value)));
                case "PERSONAL_REVENUE" -> userKpi.setPersonalRevenue(new BigDecimal(sanitizeNumericString(value)));
            }
            userKpiRepository.save(userKpi);
            System.out.printf("Сохранено [%s] для пользователя [%s]: %s\n", category, rawUser, value);
        } catch (NumberFormatException e){
            System.err.printf("Ошибка парсинга [%s] для [%s]: %s\n", category, rawUser, value);
        }
    }

    private Optional<UserKpi> getUserKpiByNameAndLocation(String rawUser){
        String[] nameAndLocation = extractNameAndLocation(rawUser);
        String name = nameAndLocation[0];
        String location = nameAndLocation[1];

        return userRepository.findAll().stream()
                .filter( user -> user.getName().equalsIgnoreCase(name)
                        && user.getLocation() != null
                        && user.getLocation().getName().equalsIgnoreCase(location))
                .findFirst()
                .map( user -> userKpiRepository.findById(user.getId()).orElseGet( () -> {
                    UserKpi newKpi = new UserKpi();
                    newKpi.setUser(user);
                    return userKpiRepository.save(newKpi);
                }));
    }

    private void handleLocationKpiByName(String category, String rawLocation, String value){
        Optional<LocationKpi> optKpi = getLocationKpiByName(rawLocation.trim());

        if(optKpi.isEmpty()) {
            System.err.printf("Локация не найдена: %s\n", rawLocation);
            return;
        }

        LocationKpi locationKpi = optKpi.get();

        try{
            switch(category){
                case "ACTUAL_INCOME" -> locationKpi.setActualIncome(new BigDecimal(sanitizeNumericString(value)));
                case "LOCATION_PLAN" -> locationKpi.setLocationPlan(new BigDecimal(sanitizeNumericString(value)));
                case "MAX_DAILY_REVENUE" -> locationKpi.setMaxDailyRevenue(new BigDecimal(sanitizeNumericString(value)));
                case "PLAN_COMPLETION_PERCENT" -> locationKpi.setPlanCompletionPercent(Double.valueOf(sanitizeNumericString(value)));
                case "REMAINING_TO_PLAN" -> locationKpi.setLocationRemainingToPlan(new BigDecimal(sanitizeNumericString(value)));
            }
            locationKpiRepository.save(locationKpi);
            System.out.printf("Сохранено [%s] для локации [%s]: %s\n", category, rawLocation, value);
        } catch (NumberFormatException e){
            System.err.printf("Ошибка парсинга [%s] для локации [%s]: %s\n", category, rawLocation, value);
        }
    }

    private Optional<LocationKpi> getLocationKpiByName(String locationName){
        return locationRepository.findAll().stream()
                .filter( location -> location.getName().equalsIgnoreCase(locationName))
                .findFirst()
                .map( location -> locationKpiRepository.findById(location.getId())
                        .orElseGet( () -> {
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

    private String sanitizeNumericString(String value) {
        return value.replace("\u00A0", "")
                .replace(" ", "")
                .replace(",", ".")
                .replace("%", "")
                .replace("₽", "")
                .trim();
    }
}
