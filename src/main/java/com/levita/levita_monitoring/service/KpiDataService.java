package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.integration.enums.SheetsRanges;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
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

    @Autowired
    public KpiDataService(UserKpiRepository userKpiRepository, UserRepository userRepository) {
        this.userKpiRepository = userKpiRepository;
        this.userRepository = userRepository;
    }

    private void handleMainSalaryPart(int userId, String value) {
        try{
            BigDecimal mainSalaryPart = new BigDecimal(value);

            Optional<User> optUser = userRepository.findById((long) userId);
            if (optUser.isEmpty()) {
                System.out.printf("Пользователь с id %d не найден", userId);
                return;
            }
            User user = optUser.get();
            Optional<UserKpi> optKpi = userKpiRepository.findByUser(user);
            UserKpi userKpi = optKpi.orElseGet( () -> {
                UserKpi newKpi = new UserKpi();
                newKpi.setUser(user);
                return newKpi;
            });
            userKpi.setMainSalaryPart(mainSalaryPart);
            userKpiRepository.save(userKpi);
            System.out.printf("Сохранен mainSalaryPart для пользователя с id %d: %s", userId, value);
        } catch (NumberFormatException e){
            System.err.printf("Неверный формат для MAIN_SALARY_PART_%d: %s", userId, value);
        }
    }

    private void handleConversionRate(int userId, String value){
        try{
            Double conversion = Double.valueOf(value);

            Optional<User> optUser = userRepository.findById((long) userId);
            if(optUser.isEmpty()){
                System.out.printf("Пользователь с индексом %s не найден", userId);
                return;
            }
            User user = optUser.get();

            Optional<UserKpi> optKpi = userKpiRepository.findByUser(user);
            UserKpi userKpi = optKpi.orElseGet( () -> {
                UserKpi newKpi = new UserKpi();
                newKpi.setUser(user);
                return newKpi;
            });
            userKpi.setConversionRate(conversion);
            userKpiRepository.save(userKpi);

            System.out.printf("Сохранена conversionRate для пользователя с индексом %d: %.1f", userId, conversion);
        } catch (NumberFormatException e){
            System.err.printf("Неверный формат для CONVERSATION_RATE_%d: %s", userId, value);
        }
    }
}
