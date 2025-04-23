package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.AdminDto;
import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.service.DashboardService;
import org.springframework.web.bind.annotation.*;

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
    public DashboardDto getAdminDashboard(@PathVariable Long userId) {
        return dashboardService.getDashboardByUserId(userId);
    }

    @GetMapping("/sorted")
    public List<AdminDto> getAdminsSorted(
            @RequestParam(required = false) String primarySort,
            @RequestParam(required = false) String primaryOrder,
            @RequestParam(required = false) String secondarySort,
            @RequestParam(required = false) String secondaryOrder
    ) {
        return dashboardService.getAllAdminsSorted(primarySort, primaryOrder, secondarySort, secondaryOrder);
    }
}
