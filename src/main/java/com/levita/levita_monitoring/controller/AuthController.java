package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Map.of(
                "id", userDetails.getUser().getId(),
                "username", userDetails.getUsername(),
                "role", userDetails.getUser().getRole()
        );
    }
}
