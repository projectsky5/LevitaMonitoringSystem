package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.security.CustomUserDetails;
import com.levita.levita_monitoring.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping()
    public DashboardDto getDashboard(@AuthenticationPrincipal CustomUserDetails userDetails){
        if(userDetails.getUser().getRole() == Role.OWNER){
            return dashboardService.getDashboardByUserId(1L);
        }

        return dashboardService.getDashboardForUser(userDetails.getUsername());
    }

}
