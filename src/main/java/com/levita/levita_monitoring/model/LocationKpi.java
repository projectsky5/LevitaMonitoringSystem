package com.levita.levita_monitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "location_kpi")
public class LocationKpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO: Подумать о введении даты

    private BigDecimal locationPlan;

    private Double locationPlanPercent;

    private BigDecimal locationRemainingToPlan;

    private BigDecimal dailyFigure;

    private BigDecimal forecast;

    private BigDecimal actualIncome;

    private Double planCompletionPercent;

    private Double planCompletionPercentDelta;

    private BigDecimal maxDailyRevenue;

    private BigDecimal avgRevenuePerDay;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;
}
