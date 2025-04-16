package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.security.CustomUserDetails;
import com.levita.levita_monitoring.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    // Для администратора
    @GetMapping()
    public ResponseEntity<DashboardDto> getDashboard(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if(user.getRole() == Role.OWNER){
            return ResponseEntity.ok(dashboardService.getDashboardByUserId(1L));
        } else {
            return ResponseEntity.ok(dashboardService.getDashboardForUser(user.getLogin()));
        }
    }

    // Для владельца
    @GetMapping("/id={id}")
    public ResponseEntity<DashboardDto> getDashboardById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if(user.getRole() != Role.OWNER){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        DashboardDto dto = dashboardService.getDashboardByUserId(id);
        return ResponseEntity.ok(dto);
    }
}
