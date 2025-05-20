package com.levita.levita_monitoring.controller;

import com.levita.levita_monitoring.controller.AdminController;
import com.levita.levita_monitoring.dto.AdminDto;
import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    @Mock DashboardService dashboardService;
    @InjectMocks AdminController controller;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllAdmins_returnsDtoList() throws Exception {
        User u = new User(); u.setId(1L);
        u.setName("Bob");
        Location loc = new Location(); loc.setName("Msk");
        u.setLocation(loc);
        UserKpi kpi = new UserKpi();
        kpi.setConversionRate(0.5);
        kpi.setPersonalRevenue(new BigDecimal("123.45"));
        u.setUserKpi(kpi);
        AdminDto dto = new AdminDto(u);

        when(dashboardService.getAllAdmins()).thenReturn(List.of(dto));

        mvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nameWithLocation").value("Bob - Msk"))
                .andExpect(jsonPath("$[0].conversionRate").value(0.5))
                .andExpect(jsonPath("$[0].personalRevenue").value(123.45));

        verify(dashboardService).getAllAdmins();
    }

    @Test
    void getAdminDashboard_byUserId() throws Exception {
        DashboardDto out = DashboardDto.builder()
                .username("Alice")
                .locationName("Spb")
                .conversionRate(0.1)
                .currentIncome(new BigDecimal("10"))
                .mainSalaryPart(new BigDecimal("5"))
                .personalRevenue(new BigDecimal("7"))
                .dayBonuses(new BigDecimal("2"))
                .dailyFigure(new BigDecimal("50"))
                .remainingToPlan(new BigDecimal("20"))
                .locationPlan(new BigDecimal("100"))
                .maxDailyRevenue(new BigDecimal("15"))
                .planCompletionPercent(0.2)
                .avgRevenuePerDay(new BigDecimal("8"))
                .actualIncome(new BigDecimal("60"))
                .build();

        when(dashboardService.getDashboardByUserId(42L)).thenReturn(out);

        mvc.perform(get("/api/admins/42/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.locationName").value("Spb"))
                .andExpect(jsonPath("$.conversionRate").value(0.1))
                .andExpect(jsonPath("$.currentIncome").value(10))
                .andExpect(jsonPath("$.planCompletionPercent").value(0.2));

        verify(dashboardService).getDashboardByUserId(42L);
    }

    @Test
    void getAdminsSorted_withParams() throws Exception {
        when(dashboardService.getAllAdminsSorted("conversionRate","desc","personalRevenue","asc"))
                .thenReturn(List.of());

        mvc.perform(get("/api/admins/sorted")
                        .param("primarySort","conversionRate")
                        .param("primaryOrder","desc")
                        .param("secondarySort","personalRevenue")
                        .param("secondaryOrder","asc"))
                .andExpect(status().isOk());

        verify(dashboardService)
                .getAllAdminsSorted("conversionRate","desc","personalRevenue","asc");
    }
}