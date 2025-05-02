package com.levita.levita_monitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_kpi")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKpi {

    @Id
    private Long id;

    private Double conversionRate;
    private BigDecimal currentIncome;
//    private BigDecimal plannedIncome;
    private BigDecimal mainSalaryPart;
    private BigDecimal personalRevenue;
    private BigDecimal dayBonuses;


    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;


    public Double getConversionRate() {
        return this.conversionRate;
    }

    public BigDecimal getCurrentIncome() {
        return this.currentIncome;
    }

//    public BigDecimal getPlannedIncome() {
//        return this.plannedIncome;
//    }

    public BigDecimal getMainSalaryPart() {
        return this.mainSalaryPart;
    }

    public BigDecimal getPersonalRevenue() {
        return this.personalRevenue;
    }

    public BigDecimal getDayBonuses() {
        return this.dayBonuses;
    }

    public User getUser() {
        return this.user;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public void setCurrentIncome(BigDecimal currentIncome) {
        this.currentIncome = currentIncome;
    }

//    public void setPlannedIncome(BigDecimal plannedIncome) {
//        this.plannedIncome = plannedIncome;
//    }

    public void setMainSalaryPart(BigDecimal mainSalaryPart) {
        this.mainSalaryPart = mainSalaryPart;
    }

    public void setPersonalRevenue(BigDecimal personalRevenue) {
        this.personalRevenue = personalRevenue;
    }

    public void setDayBonuses(BigDecimal dayBonuses) {
        this.dayBonuses = dayBonuses;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserKpi)) return false;
        final UserKpi other = (UserKpi) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$conversionRate = this.getConversionRate();
        final Object other$conversionRate = other.getConversionRate();
        if (this$conversionRate == null ? other$conversionRate != null : !this$conversionRate.equals(other$conversionRate))
            return false;
        final Object this$currentIncome = this.getCurrentIncome();
        final Object other$currentIncome = other.getCurrentIncome();
        if (this$currentIncome == null ? other$currentIncome != null : !this$currentIncome.equals(other$currentIncome))
            return false;
//        final Object this$plannedIncome = this.getPlannedIncome();
//        final Object other$plannedIncome = other.getPlannedIncome();
//        if (this$plannedIncome == null ? other$plannedIncome != null : !this$plannedIncome.equals(other$plannedIncome))
//            return false;
        final Object this$mainSalaryPart = this.getMainSalaryPart();
        final Object other$mainSalaryPart = other.getMainSalaryPart();
        if (this$mainSalaryPart == null ? other$mainSalaryPart != null : !this$mainSalaryPart.equals(other$mainSalaryPart))
            return false;
        final Object this$personalRevenue = this.getPersonalRevenue();
        final Object other$personalRevenue = other.getPersonalRevenue();
        if (this$personalRevenue == null ? other$personalRevenue != null : !this$personalRevenue.equals(other$personalRevenue))
            return false;
        final Object this$dayBonuses = this.getDayBonuses();
        final Object other$dayBonuses = other.getDayBonuses();
        if (this$dayBonuses == null ? other$dayBonuses != null : !this$dayBonuses.equals(other$dayBonuses))
            return false;
        final Object this$user = this.getUser();
        final Object other$user = other.getUser();
        if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserKpi;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $conversionRate = this.getConversionRate();
        result = result * PRIME + ($conversionRate == null ? 43 : $conversionRate.hashCode());
        final Object $currentIncome = this.getCurrentIncome();
        result = result * PRIME + ($currentIncome == null ? 43 : $currentIncome.hashCode());
//        final Object $plannedIncome = this.getPlannedIncome();
//        result = result * PRIME + ($plannedIncome == null ? 43 : $plannedIncome.hashCode());
        final Object $mainSalaryPart = this.getMainSalaryPart();
        result = result * PRIME + ($mainSalaryPart == null ? 43 : $mainSalaryPart.hashCode());
        final Object $personalRevenue = this.getPersonalRevenue();
        result = result * PRIME + ($personalRevenue == null ? 43 : $personalRevenue.hashCode());
        final Object $dayBonuses = this.getDayBonuses();
        result = result * PRIME + ($dayBonuses == null ? 43 : $dayBonuses.hashCode());
        final Object $user = this.getUser();
        result = result * PRIME + ($user == null ? 43 : $user.hashCode());
        return result;
    }

    public String toString() {
        return "UserKpi(conversionRate=" + this.getConversionRate() + ", currentIncome=" + this.getCurrentIncome() /*+ ", plannedIncome=" + this.getPlannedIncome()*/ + ", mainSalaryPart=" + this.getMainSalaryPart() + ", personalRevenue=" + this.getPersonalRevenue() + ", dayBonuses=" + this.getDayBonuses() + ", user=" + this.getUser() + ")";
    }
}
