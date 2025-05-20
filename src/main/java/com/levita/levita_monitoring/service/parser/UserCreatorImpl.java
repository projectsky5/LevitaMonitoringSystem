package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserCreatorImpl implements UserCreator {

    private static final Logger log = LoggerFactory.getLogger(UserCreatorImpl.class);

    private final NameAndLocationParser nameParser;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CredentialService credService;
    private final PasswordEncoder passwordEncoder;


    public UserCreatorImpl(NameAndLocationParser nameParser,
                           LocationRepository locationRepository,
                           UserRepository userRepository,
                           CredentialService credService,
                           PasswordEncoder passwordEncoder) {
        this.nameParser = nameParser;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.credService = credService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean createIfNotExists(String rawUser) {
        String[] nameAndLocation = nameParser.parse(rawUser);
        String name = nameAndLocation[0];
        String locName = nameAndLocation[1];

        if (name.isBlank() || locName.isBlank()) {
            log.warn("Неправильный формат пользователя [{} ({})]", name, locName);
            return false;
        }
        // 1) найти или создать Location
        log.debug("Поиск или создание локации: [{}]", locName);
        Location loc = locationRepository.findAll().stream()
                .filter(l -> l.getName().equalsIgnoreCase(locName))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Создание новой локации: [{}]", locName);
                    Location location = new Location();
                    location.setName(locName);
                    return locationRepository.save(location);
                });

        // 2) проверка существующего юзера
        log.debug("Проверка на наличие пользователя [{}] в локации [{}]", name, locName);
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getName().equalsIgnoreCase(name)
                        && u.getLocation() != null
                        && u.getLocation().getName().equalsIgnoreCase(locName));
        if (exists) {
            log.debug("Пользователь [{}] уже существует в [{}]", name, locName);
            return false;
        }
        // 3) генерим креды, сохраняем
        log.info("Cоздание сredentials для нового пользователя [{}] в локации [{}]", name, locName);
        Map<String, String> creds = credService.generate(name, locName);
        User user = new User();
        user.setName(name);
        user.setLogin(creds.get("login"));
        user.setPassword(passwordEncoder.encode(creds.get("password")));
        user.setLocation(loc);
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        log.info("Пользователь [{}] сохранен в [{}]", name, locName);
        return true;
    }
}
