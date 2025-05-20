package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.controller.AuthController;
import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.security.CustomUserDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthControllerTest {
    private AuthController ctrl = new AuthController();

    @Test
    void me_returnsMapFromPrincipal() {
        User u = new User(); u.setId(7L); u.setLogin("joe"); u.setRole(Role.ADMIN);
        CustomUserDetails cd = new CustomUserDetails(u);

        var map = ctrl.getCurrentUser(cd);

        assertEquals(7L, map.get("id"));
        assertEquals("joe", map.get("username"));
        assertEquals(Role.ADMIN, map.get("role"));
    }
}