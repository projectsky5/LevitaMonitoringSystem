package com.levita.levita_monitoring.security;

import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.repository.UserRepository;
import com.levita.levita_monitoring.security.CustomUserDetailsService;
import com.levita.levita_monitoring.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepo;
    @InjectMocks CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUser_existingLogin_returnsCustomUserDetails() {
        User u = new User();
        u.setLogin("bob");
        u.setPassword("pass");
        when(userRepo.findByLogin("bob")).thenReturn(Optional.of(u));

        var userDetails = service.loadUserByUsername("bob");
        assertTrue(userDetails instanceof CustomUserDetails);
        CustomUserDetails cud = (CustomUserDetails) userDetails;
        assertEquals("bob", cud.getUsername());
        assertEquals("pass", cud.getPassword());

        verify(userRepo).findByLogin("bob");
    }

    @Test
    void loadUser_unknownLogin_throwsUsernameNotFound() {
        when(userRepo.findByLogin("nope")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("nope"));
        verify(userRepo).findByLogin("nope");
    }
}