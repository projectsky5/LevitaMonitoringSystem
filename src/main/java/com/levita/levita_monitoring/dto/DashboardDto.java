package com.levita.levita_monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    //user

    private String username;

    //location

    private String locationName;

    //user kpi

    private Double conversionRate;
    private BigDecimal currentIncome;
    private BigDecimal plannedIncome;
    private BigDecimal mainSalaryPart;
    private BigDecimal personalRevenue;
    private BigDecimal dayBonuses;

    //location kpi

    private BigDecimal dailyFigure;
    private BigDecimal remainingToPlan;
    private BigDecimal locationPlan;
    private BigDecimal maxDailyRevenue;
    private Double planCompletionPercent;
    private BigDecimal avgRevenuePerDay;
    private BigDecimal actualIncome;
}
