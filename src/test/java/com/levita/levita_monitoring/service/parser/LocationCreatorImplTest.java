package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.service.SanitizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationCreatorImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SanitizationService sanitizationService;

    @InjectMocks
    private LocationCreatorImpl creator;

    @Test
    void createIfNotExists_whenSanitizedBlank_returnsFalseAndNoRepoCalls() {
        String raw = "   ";
        when(sanitizationService.sanitize(raw)).thenReturn("");

        boolean result = creator.createIfNotExists(raw);

        assertFalse(result);
        verify(sanitizationService).sanitize(raw);
        verifyNoInteractions(locationRepository);
    }

    @Test
    void createIfNotExists_whenAlreadyExists_returnsFalseAndDoesNotSave() {
        String raw = "  Loc1  ";
        String name = "Loc1";

        when(sanitizationService.sanitize(raw)).thenReturn(name);
        Location existing = new Location();
        existing.setName(name);
        when(locationRepository.findAll()).thenReturn(List.of(existing));

        boolean result = creator.createIfNotExists(raw);

        assertFalse(result);
        verify(sanitizationService).sanitize(raw);
        verify(locationRepository).findAll();
        verify(locationRepository, never()).save(any());
    }

    @Test
    void createIfNotExists_whenNotExists_savesAndReturnsTrue() {
        String raw = " NewLoc ";
        String name = "NewLoc";

        when(sanitizationService.sanitize(raw)).thenReturn(name);
        when(locationRepository.findAll()).thenReturn(Collections.emptyList());

        boolean result = creator.createIfNotExists(raw);

        assertTrue(result);
        verify(sanitizationService).sanitize(raw);
        verify(locationRepository).findAll();
        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());
        assertEquals(name, captor.getValue().getName());
    }
}