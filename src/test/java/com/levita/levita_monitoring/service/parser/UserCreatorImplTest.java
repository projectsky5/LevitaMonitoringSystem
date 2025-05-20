package com.levita.levita_monitoring.service.parser;

import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.repository.LocationRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserCreatorImplTest {

    @Mock NameAndLocationParser nameParser;
    @Mock LocationRepository locationRepository;
    @Mock UserRepository userRepository;
    @Mock CredentialService credService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserCreatorImpl creator;

    @Test
    void createIfNotExists_blankNameOrLocation_returnsFalse() {
        when(nameParser.parse("bad")).thenReturn(new String[]{ "", "" });
        assertFalse(creator.createIfNotExists("bad"));
        verify(nameParser).parse("bad");
        verifyNoInteractions(locationRepository, userRepository, credService, passwordEncoder);
    }

    @Test
    void createIfNotExists_blankLocation_returnsFalse() {
        when(nameParser.parse("Alice")).thenReturn(new String[]{ "Alice", "" });
        assertFalse(creator.createIfNotExists("Alice"));
        verify(nameParser).parse("Alice");
        verifyNoInteractions(locationRepository, userRepository, credService, passwordEncoder);
    }

    @Test
    void createIfNotExists_userAlreadyExists_returnsFalse() {
        when(nameParser.parse("Bob(Paris)")).thenReturn(new String[]{ "Bob", "Paris" });
        Location loc = new Location(); loc.setName("Paris");
        when(locationRepository.findAll()).thenReturn(List.of(loc));
        User existing = new User(); existing.setName("Bob"); existing.setLocation(loc);
        when(userRepository.findAll()).thenReturn(List.of(existing));

        assertFalse(creator.createIfNotExists("Bob(Paris)"));
        verify(locationRepository).findAll();
        verify(userRepository).findAll();
        verify(credService, never()).generate(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createIfNotExists_newUser_existingLocation_returnsTrue() {
        when(nameParser.parse("Carol(Berlin)")).thenReturn(new String[]{ "Carol", "Berlin" });
        Location loc = new Location(); loc.setName("Berlin");
        when(locationRepository.findAll()).thenReturn(List.of(loc));
        when(userRepository.findAll()).thenReturn(List.of());
        Map<String,String> creds = Map.of("login","carolL","password","pwd");
        when(credService.generate("Carol","Berlin")).thenReturn(creds);
        when(passwordEncoder.encode("pwd")).thenReturn("encPwd");

        assertTrue(creator.createIfNotExists("Carol(Berlin)"));
        ArgumentCaptor<User> uc = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(uc.capture());
        User saved = uc.getValue();
        assertEquals("Carol", saved.getName());
        assertEquals("carolL", saved.getLogin());
        assertEquals("encPwd", saved.getPassword());
        assertSame(loc, saved.getLocation());
        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    void createIfNotExists_newUser_newLocation_returnsTrue() {
        when(nameParser.parse("Dave(Tokyo)")).thenReturn(new String[]{ "Dave", "Tokyo" });
        when(locationRepository.findAll()).thenReturn(List.of());
        Location newLoc = new Location(); newLoc.setName("Tokyo");
        when(locationRepository.save(any(Location.class))).thenReturn(newLoc);
        when(userRepository.findAll()).thenReturn(List.of());
        Map<String,String> creds = Map.of("login","daveL","password","pw");
        when(credService.generate("Dave","Tokyo")).thenReturn(creds);
        when(passwordEncoder.encode("pw")).thenReturn("encPw");

        assertTrue(creator.createIfNotExists("Dave(Tokyo)"));
        ArgumentCaptor<Location> lc = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(lc.capture());
        assertEquals("Tokyo", lc.getValue().getName());

        ArgumentCaptor<User> uc = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(uc.capture());
        User saved = uc.getValue();
        assertEquals("Dave", saved.getName());
        assertEquals("daveL", saved.getLogin());
        assertEquals("encPw", saved.getPassword());
        assertSame(newLoc, saved.getLocation());
        assertEquals(Role.ADMIN, saved.getRole());
    }
}