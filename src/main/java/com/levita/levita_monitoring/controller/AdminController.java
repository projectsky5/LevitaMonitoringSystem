package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.AdminDto;
import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public List<AdminDto> getAllAdmins() {
        return dashboardService.getAllAdmins();
    }

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<DashboardDto> getAdminDashboard(@PathVariable Long userId) {
        DashboardDto dto = dashboardService.getDashboardByUserId(userId);
        return ResponseEntity.ok(dto);
    }
}
