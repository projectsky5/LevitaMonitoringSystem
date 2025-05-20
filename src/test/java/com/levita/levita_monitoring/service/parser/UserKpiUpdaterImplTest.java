package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.enums.Kpi;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import com.levita.levita_monitoring.service.SanitizationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserKpiUpdaterImplTest {

    @Mock NameAndLocationParser parser;
    @Mock UserRepository userRepository;
    @Mock UserKpiRepository userKpiRepository;
    @Mock SanitizationService sanitizationService;
    @InjectMocks UserKpiUpdaterImpl updater;

    private final String rawUser = "Alice(Office)";

    private User alice;
    private UserKpi existingKpi;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(42L);
        alice.setName("Alice");
        alice.setLocation(new com.levita.levita_monitoring.model.Location());
        alice.getLocation().setName("Office");

        existingKpi = new UserKpi();
        existingKpi.setUser(alice);

        lenient().when(parser.parse(rawUser)).thenReturn(new String[]{"Alice","Office"});
    }

    @Test
    void update_unknownTopic_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updater.update("no-such-topic", rawUser, "123"));
    }

    @Test
    void update_userNotFound_throwsEntityNotFoundException() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class,
                () -> updater.update(Kpi.CONVERSION.getTopic(), rawUser, "0.5"));
    }

    @Test
    void update_invalidValue_throwsIllegalArgumentException() {
        when(userRepository.findAll()).thenReturn(List.of(alice));
        when(userKpiRepository.findById(42L)).thenReturn(Optional.of(existingKpi));
        when(sanitizationService.sanitize("bad")).thenReturn("bad");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updater.update(Kpi.CONVERSION.getTopic(), rawUser, "bad"));
        assertTrue(ex.getMessage().contains("Неверное значение KPI"));
        assertTrue(ex.getCause() instanceof NumberFormatException);
    }

    @Test
    void update_existingKpi_conversionRateAndSavesOnce() {
        when(userRepository.findAll()).thenReturn(List.of(alice));
        when(userKpiRepository.findById(42L)).thenReturn(Optional.of(existingKpi));
        when(sanitizationService.sanitize("0.75")).thenReturn("0.75");

        updater.update(Kpi.CONVERSION.getTopic(), rawUser, "0.75");

        assertEquals(0.75, existingKpi.getConversionRate());
        verify(userKpiRepository, times(1)).save(existingKpi);
    }

    @Test
    void update_existingKpi_mainSalaryPartAndSavesOnce() {
        when(userRepository.findAll()).thenReturn(List.of(alice));
        when(userKpiRepository.findById(42L)).thenReturn(Optional.of(existingKpi));
        when(sanitizationService.sanitize("1234.56")).thenReturn("1234.56");

        updater.update(Kpi.MAIN_SALARY_PART.getTopic(), rawUser, "1234.56");

        assertEquals(new BigDecimal("1234.56"), existingKpi.getMainSalaryPart());
        verify(userKpiRepository, times(1)).save(existingKpi);
    }

    @Test
    void update_newKpi_createsThenSetsBonusAndSavesTwice() {
        when(userRepository.findAll()).thenReturn(List.of(alice));
        when(userKpiRepository.findById(42L)).thenReturn(Optional.empty());
        when(userKpiRepository.save(any(UserKpi.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sanitizationService.sanitize("50")).thenReturn("50");

        updater.update(Kpi.BONUSES.getTopic(), rawUser, "50");

        ArgumentCaptor<UserKpi> cap = ArgumentCaptor.forClass(UserKpi.class);
        verify(userKpiRepository, times(2)).save(cap.capture());

        UserKpi created = cap.getAllValues().get(0);
        assertEquals(alice, created.getUser());

        UserKpi updated = cap.getAllValues().get(1);
        assertEquals(new BigDecimal("50"), updated.getDayBonuses());
    }
}