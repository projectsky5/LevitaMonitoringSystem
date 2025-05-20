package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.security.CustomUserDetails;
import com.levita.levita_monitoring.service.DashboardService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardControllerTest {
    private DashboardService svc = mock(DashboardService.class);
    private DashboardController c = new DashboardController(svc);

    @Test
    void ownerRole_usesById() {
        User u = new User(); u.setId(9L); u.setLogin("x"); u.setRole(Role.OWNER);
        CustomUserDetails cd = new CustomUserDetails(u);
        DashboardDto dto = new DashboardDto(); dto.setUsername("OO");
        when(svc.getDashboardByUserId(1L)).thenReturn(dto);

        DashboardDto out = c.getDashboard(cd);

        assertSame(dto, out);
        verify(svc).getDashboardByUserId(1L);
    }

    @Test
    void adminRole_usesByLogin() {
        User u = new User(); u.setLogin("bob"); u.setRole(Role.ADMIN);
        CustomUserDetails cd = new CustomUserDetails(u);
        DashboardDto dto = new DashboardDto(); dto.setUsername("BB");
        when(svc.getDashboardForUser("bob")).thenReturn(dto);

        DashboardDto out = c.getDashboard(cd);

        assertSame(dto, out);
        verify(svc).getDashboardForUser("bob");
    }
}