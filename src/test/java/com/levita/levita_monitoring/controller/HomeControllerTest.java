package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.security.CustomUserDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeControllerTest {
    HomeController c = new HomeController();

    @Test
    void index_forwardsToIndex() {
        assertEquals("forward:/index.html", c.index());
    }

    @Test
    void dashboardForAdmin_owner() {
        CustomUserDetails cd = mock(CustomUserDetails.class);
        when(cd.getUser()).thenReturn(new User(){ { setRole(Role.OWNER);} });
        assertEquals("forward:/index.html", c.dashboardForAdmin(cd, 123L));
    }

    @Test
    void dashboardForAdmin_notOwner() {
        CustomUserDetails cd = mock(CustomUserDetails.class);
        when(cd.getUser()).thenReturn(new User(){ { setRole(Role.ADMIN);} });
        assertEquals("forward:/error.html", c.dashboardForAdmin(cd, 123L));
    }

    @Test
    void filter_owner() {
        CustomUserDetails cd = mock(CustomUserDetails.class);
        when(cd.getUser()).thenReturn(new User(){ { setRole(Role.OWNER);} });
        assertEquals("forward:/filter.html", c.filter(cd));
    }

    @Test
    void filter_notOwner() {
        CustomUserDetails cd = mock(CustomUserDetails.class);
        when(cd.getUser()).thenReturn(new User(){ { setRole(Role.ADMIN);} });
        assertEquals("forward:/error.html", c.filter(cd));
    }
}