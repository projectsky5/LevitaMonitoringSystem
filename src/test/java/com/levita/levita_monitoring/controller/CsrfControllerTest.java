package com.levita.levita_monitoring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CsrfControllerTest {
    private CsrfController ctrl = new CsrfController();
    @Test
    void csrf_returnsSameToken() {
        CsrfToken token = mock(CsrfToken.class);
        assertSame(token, ctrl.csrf(token));
    }
}