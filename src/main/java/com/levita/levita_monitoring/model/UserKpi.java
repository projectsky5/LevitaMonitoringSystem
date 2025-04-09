package com.levita.levita_monitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_kpi")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserKpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double conversionRate;
    private BigDecimal currentIncome;
    private BigDecimal plannedIncome;
    private BigDecimal mainSalaryPart;
    private BigDecimal personalRevenue;
    private BigDecimal dayBonuses;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
}
