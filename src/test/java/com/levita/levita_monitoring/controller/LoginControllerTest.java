package com.levita.levita_monitoring.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {
    LoginController c = new LoginController();
    @Test
    void login_forwardsToLogin() {
        assertEquals("forward:/login.html", c.login());
    }
}