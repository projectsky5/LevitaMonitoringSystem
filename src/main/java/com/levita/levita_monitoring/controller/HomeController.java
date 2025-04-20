package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/dashboard")
    public String index(){
        return "forward:/index.html";
    }

    @GetMapping("/dashboard/{id}")
    public String dashboardForAdmin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ){
        if(userDetails.getUser().getRole() != Role.OWNER){
            return "forward:/error.html";
        }
        return "forward:/index.html";
    }
}
