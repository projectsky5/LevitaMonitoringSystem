package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.enums.Kpi;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.UserRepository;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.service.SanitizationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class UserKpiUpdaterImpl implements UserKpiUpdater {

    private static final Logger log = LoggerFactory.getLogger(UserKpiUpdaterImpl.class);

    private final NameAndLocationParser parser;
    private final UserRepository userRepository;
    private final UserKpiRepository userKpiRepository;
    private final SanitizationService sanitizationService;

    // Простая мапа topic -> сеттер
    private final Map<String, BiConsumer<UserKpi, String>> setters = new HashMap<>();

    public UserKpiUpdaterImpl(NameAndLocationParser parser,
                              UserRepository userRepository,
                              UserKpiRepository userKpiRepository,
                              SanitizationService sanitizationService) {
        this.parser = parser;
        this.userRepository = userRepository;
        this.userKpiRepository = userKpiRepository;
        this.sanitizationService = sanitizationService;

        // Инициализируем сеттеры для каждой метрики
        setters.put(Kpi.CONVERSION.getTopic(), (kpi, raw) ->
                kpi.setConversionRate(
                        Double.parseDouble(sanitizationService.sanitize(raw))
                )
        );
        setters.put(Kpi.MAIN_SALARY_PART.getTopic(), (kpi, raw) ->
                kpi.setMainSalaryPart(
                        new BigDecimal(sanitizationService.sanitize(raw))
                )
        );
        setters.put(Kpi.USER_REVENUE.getTopic(), (kpi, raw) ->
                kpi.setPersonalRevenue(
                        new BigDecimal(sanitizationService.sanitize(raw))
                )
        );
        setters.put(Kpi.USER_INCOME.getTopic(), (kpi, raw) ->
                kpi.setCurrentIncome(
                        new BigDecimal(sanitizationService.sanitize(raw))
                )
        );
        setters.put(Kpi.BONUSES.getTopic(), (kpi, raw) ->
                kpi.setDayBonuses(
                        new BigDecimal(sanitizationService.sanitize(raw))
                )
        );
    }

    @Override
    public void update(String topic, String rawUser, String rawValue) {
        // 1) находим нужный сеттер
        log.debug("Начало обновления KPI [{}] для пользователя {}", topic, rawUser);
        BiConsumer<UserKpi, String> setter = setters.get(topic);
        if (setter == null) {
            log.error("Неизвестная KPI категория: {}. Остановка обновления", topic);
            throw new IllegalArgumentException("Unknown KPI topic: " + topic);
        }

        // 2) парсим "Имя (Локация)"
        String[] parts = parser.parse(rawUser);
        String name = parts[0];
        String location = parts[1];

        // 3) ищем пользователя, иначе бросаем
        var user = userRepository.findAll().stream()
                .filter(u -> u.getName().equalsIgnoreCase(name)
                        && u.getLocation() != null
                        && u.getLocation().getName().equalsIgnoreCase(location))
                .findFirst();
        if (user.isEmpty()){
            log.warn("Пользователь [{}] в локации [{}] не найден. Пропуск обновления KPI", name, location);
            throw new EntityNotFoundException("Пользователь не найден: " + rawUser);
        }

        // 4) получаем или создаём UserKpi
        UserKpi userKpi = userKpiRepository.findById(user.get().getId())
                .orElseGet(() -> {
                    log.info("Создание нового KPI для пользователя [{}]", rawUser);
                    var newKpi = new UserKpi();
                    newKpi.setUser(user.get());
                    return userKpiRepository.save(newKpi);
                });

        // 5) вызываем сеттер и сохраняем
        try {
            setter.accept(userKpi, rawValue);
            log.info("Применен KPI [{}] = {} для пользователя [{}]", topic, rawValue, rawUser);
        } catch (NumberFormatException ex) {
            log.error("Неверное значение KPI [{}] для категории [{}]. Ошибка: {}", rawValue, topic, ex.getMessage());
            throw new IllegalArgumentException("Неверное значение KPI: [{}] " + rawValue, ex);
        }
        userKpiRepository.save(userKpi);
        log.debug("Закончено обновление KPI [{}] для пользователя [{}]", topic, rawValue);
    }
}