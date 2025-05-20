package com.levita.levita_monitoring.service;

import com.levita.levita_monitoring.dto.AdminDto;
import com.levita.levita_monitoring.dto.DashboardDto;
import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.Location;
import com.levita.levita_monitoring.model.LocationKpi;
import com.levita.levita_monitoring.model.User;
import com.levita.levita_monitoring.model.UserKpi;
import com.levita.levita_monitoring.repository.LocationKpiRepository;
import com.levita.levita_monitoring.repository.UserKpiRepository;
import com.levita.levita_monitoring.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserKpiRepository userKpiRepository;

    @Mock
    private LocationKpiRepository locationKpiRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    void getDashboardForUser_HappyPath() {
        Long userId = 1L;
        Long locationId = 2L;
        String login = "alice";
        String name = "Alice";
        String locName = "HQ";

        Location loc = mock(Location.class);
        when(loc.getId()).thenReturn(locationId);
        when(loc.getName()).thenReturn(locName);

        UserKpi uk = new UserKpi();
        uk.setConversionRate(0.5);
        uk.setCurrentIncome(new BigDecimal("1000"));
        uk.setMainSalaryPart(new BigDecimal("800"));
        uk.setPersonalRevenue(new BigDecimal("200"));
        uk.setDayBonuses(new BigDecimal("50"));

        LocationKpi lk = new LocationKpi();
        lk.setDailyFigure(new BigDecimal("1500"));
        lk.setLocationRemainingToPlan(new BigDecimal("300"));
        lk.setLocationPlan(new BigDecimal("1800"));
        lk.setMaxDailyRevenue(new BigDecimal("2500"));
        lk.setPlanCompletionPercent(83.33);
        lk.setAvgRevenuePerDay(new BigDecimal("1200"));
        lk.setActualIncome(new BigDecimal("1100"));

        User user = User.builder()
                .id(userId)
                .login(login)
                .name(name)
                .location(loc)
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(userKpiRepository.findById(userId)).thenReturn(Optional.of(uk));
        when(locationKpiRepository.findById(locationId)).thenReturn(Optional.of(lk));

        DashboardDto dto = service.getDashboardForUser(login);

        assertEquals(name, dto.getUsername());
        assertEquals(locName, dto.getLocationName());
        assertEquals(0.5, dto.getConversionRate());
        assertEquals(new BigDecimal("1000"), dto.getCurrentIncome());
        assertEquals(new BigDecimal("800"), dto.getMainSalaryPart());
        assertEquals(new BigDecimal("200"), dto.getPersonalRevenue());
        assertEquals(new BigDecimal("50"), dto.getDayBonuses());
        assertEquals(new BigDecimal("1500"), dto.getDailyFigure());
        assertEquals(new BigDecimal("300"), dto.getRemainingToPlan());
        assertEquals(new BigDecimal("1800"), dto.getLocationPlan());
        assertEquals(new BigDecimal("2500"), dto.getMaxDailyRevenue());
        assertEquals(83.33, dto.getPlanCompletionPercent());
        assertEquals(new BigDecimal("1200"), dto.getAvgRevenuePerDay());
        assertEquals(new BigDecimal("1100"), dto.getActualIncome());
    }

    @Test
    void getDashboardForUser_UserNotFound_Throws() {
        when(userRepository.findByLogin("bob")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getDashboardForUser("bob"));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getDashboardForUser_UserKpiNotFound_Throws() {
        Long id = 5L;
        Location loc = mock(Location.class);
        when(loc.getId()).thenReturn(10L);
        User u = User.builder().id(id).login("u").location(loc).role(Role.ADMIN).build();

        when(userRepository.findByLogin("u")).thenReturn(Optional.of(u));
        lenient().when(userKpiRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getDashboardForUser("u"));
        assertEquals("User KPI not found", ex.getMessage());
    }

    @Test
    void getDashboardForUser_LocationKpiNotFound_Throws() {
        Long id = 6L;
        Long locId = 7L;
        Location loc = mock(Location.class);
        when(loc.getId()).thenReturn(locId);
        User u = User.builder().id(id).login("x").location(loc).role(Role.ADMIN).build();

        when(userRepository.findByLogin("x")).thenReturn(Optional.of(u));
        when(userKpiRepository.findById(id)).thenReturn(Optional.of(new UserKpi()));
        lenient().when(locationKpiRepository.findById(locId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getDashboardForUser("x"));
        assertEquals("Location KPI not found", ex.getMessage());
    }

    @Test
    void getDashboardByUserId_HappyPath() {
        Long userId = 11L;
        Long locId = 12L;
        Location loc = mock(Location.class);
        when(loc.getId()).thenReturn(locId);
        when(loc.getName()).thenReturn("Branch");

        UserKpi uk = new UserKpi();
        uk.setConversionRate(0.6);
        uk.setCurrentIncome(new BigDecimal("900"));
        uk.setMainSalaryPart(new BigDecimal("600"));
        uk.setPersonalRevenue(new BigDecimal("300"));
        uk.setDayBonuses(new BigDecimal("80"));

        LocationKpi lk = new LocationKpi();
        lk.setDailyFigure(new BigDecimal("1400"));
        lk.setLocationRemainingToPlan(new BigDecimal("200"));
        lk.setLocationPlan(new BigDecimal("1600"));
        lk.setMaxDailyRevenue(new BigDecimal("2400"));
        lk.setPlanCompletionPercent(87.5);
        lk.setAvgRevenuePerDay(new BigDecimal("1300"));
        lk.setActualIncome(new BigDecimal("1250"));

        User u = User.builder()
                .id(userId)
                .name("Bob")
                .location(loc)
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        when(userKpiRepository.findById(userId)).thenReturn(Optional.of(uk));
        when(locationKpiRepository.findById(locId)).thenReturn(Optional.of(lk));

        DashboardDto dto = service.getDashboardByUserId(userId);
        assertEquals("Bob", dto.getUsername());
        assertEquals("Branch", dto.getLocationName());
    }

    @Test
    void getDashboardByUserId_UserNotFound_Throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getDashboardByUserId(99L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getDashboardByUserId_UserKpiNotFound_Throws() {
        Long userId = 13L;
        Location loc = mock(Location.class);
        when(loc.getId()).thenReturn(14L);
        User u = User.builder().id(userId).location(loc).role(Role.ADMIN).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        lenient().when(userKpiRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getDashboardByUserId(userId));
        assertEquals("User KPI not found", ex.getMessage());
    }

    @Test
    void getDashboardByUserId_LocationKpiNotFound_Throws() {
        Long userId = 15L;
        Long locId = 16L;
        Location loc = mock(Location.class);
        when(loc.getId()).thenReturn(locId);
        User u = User.builder().id(userId).location(loc).role(Role.ADMIN).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        when(userKpiRepository.findById(userId)).thenReturn(Optional.of(new UserKpi()));
        lenient().when(locationKpiRepository.findById(locId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getDashboardByUserId(userId));
        assertEquals("Location KPI not found", ex.getMessage());
    }

    @Test
    void getAllAdmins_EmptyList() {
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(List.of());
        assertTrue(service.getAllAdmins().isEmpty());
    }

    @Test
    void getAllAdmins_ReturnsDtos() {
        User a1 = User.builder().id(1L).name("A1").location(mock(Location.class)).role(Role.ADMIN).build();
        User a2 = User.builder().id(2L).name("A2").location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(a1, a2));

        List<AdminDto> result = service.getAllAdmins();
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getAllAdminsSorted_PrimaryDesc() {
        UserKpi uk1 = new UserKpi(); uk1.setConversionRate(0.9);
        UserKpi uk2 = new UserKpi(); uk2.setConversionRate(0.1);
        User u1 = User.builder().id(1L).userKpi(uk1).location(mock(Location.class)).role(Role.ADMIN).build();
        User u2 = User.builder().id(2L).userKpi(uk2).location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(u2, u1));

        List<AdminDto> res = service.getAllAdminsSorted("conversionRate", "desc", null, null);
        assertEquals(0.9, res.get(0).getConversionRate());
        assertEquals(0.1, res.get(1).getConversionRate());
    }

    @Test
    void getAllAdminsSorted_PrimaryInvalidOrder_DefaultsAsc() {
        UserKpi uk1 = new UserKpi(); uk1.setConversionRate(0.8);
        UserKpi uk2 = new UserKpi(); uk2.setConversionRate(0.2);
        User u1 = User.builder().id(1L).userKpi(uk1).location(mock(Location.class)).role(Role.ADMIN).build();
        User u2 = User.builder().id(2L).userKpi(uk2).location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(u1, u2));

        List<AdminDto> res = service.getAllAdminsSorted("conversionRate", "garbage", null, null);
        assertEquals(0.2, res.get(0).getConversionRate());
        assertEquals(0.8, res.get(1).getConversionRate());
    }

    @Test
    void getAllAdminsSorted_SecondaryDesc() {
        UserKpi uk1 = new UserKpi(); uk1.setPersonalRevenue(new BigDecimal("300"));
        UserKpi uk2 = new UserKpi(); uk2.setPersonalRevenue(new BigDecimal("100"));
        User u1 = User.builder().id(1L).userKpi(uk1).location(mock(Location.class)).role(Role.ADMIN).build();
        User u2 = User.builder().id(2L).userKpi(uk2).location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(u2, u1));

        List<AdminDto> res = service.getAllAdminsSorted(null, null, "personalRevenue", "desc");
        assertEquals(new BigDecimal("300"), res.get(0).getPersonalRevenue());
        assertEquals(new BigDecimal("100"), res.get(1).getPersonalRevenue());
    }

    @Test
    void getAllAdminsSorted_PrimaryThenSecondary() {
        UserKpi uk1 = new UserKpi(); uk1.setConversionRate(0.5); uk1.setPersonalRevenue(new BigDecimal("200"));
        UserKpi uk2 = new UserKpi(); uk2.setConversionRate(0.5); uk2.setPersonalRevenue(new BigDecimal("100"));
        User u1 = User.builder().id(1L).userKpi(uk1).location(mock(Location.class)).role(Role.ADMIN).build();
        User u2 = User.builder().id(2L).userKpi(uk2).location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(u1, u2));

        List<AdminDto> res = service.getAllAdminsSorted("conversionRate", "asc", "personalRevenue", "asc");
        assertEquals(100, res.get(0).getPersonalRevenue().intValue());
        assertEquals(200, res.get(1).getPersonalRevenue().intValue());
    }

    @Test
    void getAllAdminsSorted_SecondaryEqualsPrimary_Ignored() {
        UserKpi uk1 = new UserKpi(); uk1.setConversionRate(0.4);
        UserKpi uk2 = new UserKpi(); uk2.setConversionRate(0.6);
        User u1 = User.builder().id(1L).userKpi(uk1).location(mock(Location.class)).role(Role.ADMIN).build();
        User u2 = User.builder().id(2L).userKpi(uk2).location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(u1, u2));

        List<AdminDto> res = service.getAllAdminsSorted("conversionRate", "asc", "conversionRate", "desc");
        assertEquals(0.4, res.get(0).getConversionRate());
        assertEquals(0.6, res.get(1).getConversionRate());
    }

    @Test
    void getAllAdminsSorted_InvalidFields_Ignored() {
        User u1 = User.builder().id(1L).location(mock(Location.class)).role(Role.ADMIN).build();
        User u2 = User.builder().id(2L).location(mock(Location.class)).role(Role.ADMIN).build();
        when(userRepository.findAllAdminsWithKpi(Role.ADMIN)).thenReturn(Arrays.asList(u1, u2));

        List<AdminDto> res = service.getAllAdminsSorted("foo", "asc", "bar", "desc");
        assertEquals(1L, res.get(0).getId());
        assertEquals(2L, res.get(1).getId());
    }
}