package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(Principal principal){
        DashboardDto dto = dashboardService.getDashboardForUser(principal.getName());
        return ResponseEntity.ok(dto);
    }
}
