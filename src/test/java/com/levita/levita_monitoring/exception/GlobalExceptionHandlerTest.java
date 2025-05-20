package com.levita.levita_monitoring.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler advice;

    @BeforeEach
    void setUp() {
        advice = new GlobalExceptionHandler();
    }

    @Test
    void handleUserNotFound_returns404AndBody() {
        var ex = new UsernameNotFoundException("no such user");
        ResponseEntity<?> resp = advice.handleUserNotFound(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(404);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body)
                .containsEntry("error",   "User not found")
                .containsEntry("message", "no such user");
    }

    @Test
    void handleIllegalArg_returns400AndBody() {
        var ex = new IllegalArgumentException("bad input");
        ResponseEntity<?> resp = advice.handleIllegalArg(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(400);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body)
                .containsEntry("error",   "Invalid input")
                .containsEntry("message", "bad input");
    }

    @Test
    void handleIOException_returns500AndBody() {
        var ex = new IOException("disk error");
        ResponseEntity<?> resp = advice.handleIOException(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(500);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body)
                .containsEntry("error",   "Google Sheets error")
                .containsEntry("message", "disk error");
    }

    @Test
    void handleRuntime_returns500AndBody() {
        var ex = new RuntimeException("oops");
        ResponseEntity<?> resp = advice.handleRuntime(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(500);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body)
                .containsEntry("error",   "Runtime error")
                .containsEntry("message", "oops");
    }

    @Test
    void handleValidationException_returns400AndFieldErrors() {
        var bindingResult = mock(BindingResult.class);
        var fieldError1 = new FieldError("obj", "field1", "must not be blank");
        var fieldError2 = new FieldError("obj", "field2", "must be positive");
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.List.of(fieldError1, fieldError2)
        );
        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<?> resp = advice.handleValidationException(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(400);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body).containsEntry("error", "Validation failed");

        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) body.get("fields");
        assertThat(fields)
                .containsEntry("field1", "must not be blank")
                .containsEntry("field2", "must be positive");
    }

    @Test
    void handleGeneral_returns500AndBody() {
        var ex = new Exception("something bad");
        ResponseEntity<?> resp = advice.handleGeneral(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(500);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body)
                .containsEntry("error",   "Unexpected error")
                .containsEntry("message", "something bad");
    }
}