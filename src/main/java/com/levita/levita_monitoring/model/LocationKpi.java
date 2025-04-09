//TODO: Подумать о введении даты
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

    private BigDecimal dailyFigure; //Цифра дня
    private BigDecimal locationRemainingToPlan; // Осталось до плана
    private BigDecimal locationPlan; // План локации
    private BigDecimal maxDailyRevenue; // Макс дневная выручка
    private Double planCompletionPercent; // выполнение плана %
    private BigDecimal avgRevenuePerDay; // Средняя выручка
    private BigDecimal actualIncome; // Фактический доход

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;
}
