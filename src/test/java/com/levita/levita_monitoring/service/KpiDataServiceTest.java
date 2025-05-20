package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.enums.Category;
import com.levita.levita_monitoring.enums.Kpi;
import com.levita.levita_monitoring.integration.model.RangeDescriptor;
import com.levita.levita_monitoring.service.parser.LocationCreator;
import com.levita.levita_monitoring.service.parser.UserCreator;
import com.levita.levita_monitoring.service.parser.LocationKpiUpdater;
import com.levita.levita_monitoring.service.parser.UserKpiUpdater;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiDataServiceTest {

    @Mock UserCreator userCreator;
    @Mock LocationCreator locationCreator;
    @Mock UserKpiUpdater userKpiUpdater;
    @Mock LocationKpiUpdater locationKpiUpdater;

    @InjectMocks KpiDataService service;

    @Test
    void saveDataFromSheets_whenUsersCategory_invokesUserCreatorOnly() {
        RangeDescriptor desc = mock(RangeDescriptor.class);
        when(desc.category()).thenReturn(Category.USERS.get());
        service.saveDataFromSheets(desc, "someValue");
        verify(userCreator).createIfNotExists("someValue");
        verifyNoMoreInteractions(userCreator, locationCreator, userKpiUpdater, locationKpiUpdater);
    }

    @Test
    void saveDataFromSheets_whenLocationsCategory_invokesLocationCreatorOnly() {
        RangeDescriptor desc = mock(RangeDescriptor.class);
        when(desc.category()).thenReturn(Category.LOCATIONS.get());
        service.saveDataFromSheets(desc, "locVal");
        verify(locationCreator).createIfNotExists("locVal");
        verifyNoMoreInteractions(userCreator, locationCreator, userKpiUpdater, locationKpiUpdater);
    }

    @Test
    void saveDataFromSheets_whenUnknownCategory_throwsIllegalArgument() {
        RangeDescriptor desc = mock(RangeDescriptor.class);
        when(desc.category()).thenReturn("unknown");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.saveDataFromSheets(desc, "v"));
        assertTrue(ex.getMessage().contains("Unsupported category: unknown"));
        verifyNoInteractions(userCreator, locationCreator, userKpiUpdater, locationKpiUpdater);
    }

    @Test
    void saveDataFromSheets_userKpiCategory_callsUserKpiUpdater() {
        RangeDescriptor desc = mock(RangeDescriptor.class);
        String topic = Kpi.USER_REVENUE.getTopic();
        String label = "Alice (HQ)";
        String value = "200";
        when(desc.category()).thenReturn(topic);
        when(desc.label()).thenReturn(label);

        service.saveDataFromSheets(desc, value);

        verify(userKpiUpdater).update(topic, label, value);
        verifyNoMoreInteractions(userCreator, locationCreator, userKpiUpdater, locationKpiUpdater);
    }

    @Test
    void saveDataFromSheets_locationKpiCategory_callsLocationKpiUpdater() {
        RangeDescriptor desc = mock(RangeDescriptor.class);
        String topic = Kpi.PLAN.getTopic(); // PLAN is location KPI
        String label = "Office";
        String value = "5000";
        when(desc.category()).thenReturn(topic);
        when(desc.label()).thenReturn(label);

        service.saveDataFromSheets(desc, value);

        verify(locationKpiUpdater).update(topic, label, value);
        verifyNoMoreInteractions(userCreator, locationCreator, userKpiUpdater, locationKpiUpdater);
    }
}