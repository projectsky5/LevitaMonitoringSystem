package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.enums.Kpi;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
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
class LocationKpiUpdaterImplTest {

    @Mock private LocationRepository locationRepository;
    @Mock private LocationKpiRepository kpiRepository;
    @Mock private SanitizationService sanitization;
    @InjectMocks private LocationKpiUpdaterImpl updater;

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(100L);
        location.setName("TestLoc");
        lenient().when(locationRepository.findAll()).thenReturn(List.of(location));
    }

    @Test
    void update_unknownTopic_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> updater.update("no-such-topic", "TestLoc", "123"));
    }

    @Test
    void update_locationNotFound_throwsEntityNotFoundException() {
        when(locationRepository.findAll()).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class,
                () -> updater.update(Kpi.LOCATION_INCOME.getTopic(), "OtherLoc", "123"));
    }

    @Test
    void update_invalidValue_throwsIllegalArgumentException() {
        String topic = Kpi.PLAN_COMPLETION.getTopic();
        String rawValue = "badNumber";

        LocationKpi existing = new LocationKpi();
        existing.setLocation(location);
        when(kpiRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(sanitization.sanitize(rawValue)).thenReturn(rawValue);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updater.update(topic, "TestLoc", rawValue));
        assertTrue(ex.getMessage().contains("Invalid KPI value: " + rawValue));
        assertTrue(ex.getCause() instanceof NumberFormatException);
    }

    @Test
    void update_existingKpi_updatesValueAndSavesOnce() {
        String topic = Kpi.LOCATION_INCOME.getTopic();
        String rawValue = "1500";

        LocationKpi existing = new LocationKpi();
        existing.setLocation(location);
        when(kpiRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(sanitization.sanitize(rawValue)).thenReturn(rawValue);

        updater.update(topic, "TestLoc", rawValue);

        assertEquals(new BigDecimal("1500"), existing.getActualIncome());

        verify(kpiRepository, times(1)).findById(100L);
        verify(kpiRepository, times(1)).save(existing);
    }

    @Test
    void update_newKpi_createsThenUpdatesAndSavesTwice() {
        String topic = Kpi.PLAN.getTopic();
        String rawValue = "2000";

        when(kpiRepository.findById(100L)).thenReturn(Optional.empty());
        when(kpiRepository.save(any(LocationKpi.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sanitization.sanitize(rawValue)).thenReturn(rawValue);

        updater.update(topic, "TestLoc", rawValue);

        ArgumentCaptor<LocationKpi> captor = ArgumentCaptor.forClass(LocationKpi.class);
        verify(kpiRepository, times(2)).save(captor.capture());

        LocationKpi first = captor.getAllValues().get(0);
        assertEquals(location, first.getLocation());

        LocationKpi second = captor.getAllValues().get(1);
        assertEquals(new BigDecimal("2000"), second.getLocationPlan());
    }
}