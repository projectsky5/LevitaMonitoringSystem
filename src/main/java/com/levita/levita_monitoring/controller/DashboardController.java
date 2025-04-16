package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping()
    public ResponseEntity<DashboardDto> getDashboard(){
        DashboardDto dto = dashboardService.getDashboardForUser("user_женя_варварская");
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/id={id}")
    public ResponseEntity<DashboardDto> getDashboardById(@PathVariable Long id){
        DashboardDto dto = dashboardService.getDashboardByUserId(id);
        return ResponseEntity.ok(dto);
    }
}
