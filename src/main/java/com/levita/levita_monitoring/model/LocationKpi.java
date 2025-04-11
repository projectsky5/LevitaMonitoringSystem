//TODO: Подумать о введении даты
package com.levita.levita_monitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "location_kpi")
public class LocationKpi {

    @Id
    private Long id;

    private BigDecimal dailyFigure; //Цифра дня
    private BigDecimal locationRemainingToPlan; // Осталось до плана
    private BigDecimal locationPlan; // План локации
    private BigDecimal maxDailyRevenue; // Макс дневная выручка
    private Double planCompletionPercent; // выполнение плана %
    private BigDecimal avgRevenuePerDay; // Средняя выручка
    private BigDecimal actualIncome; // Фактический доход

    @OneToOne
    @MapsId
    @JoinColumn(name = "location_id")
    private Location location;


    public BigDecimal getDailyFigure() {
        return this.dailyFigure;
    }

    public BigDecimal getLocationRemainingToPlan() {
        return this.locationRemainingToPlan;
    }

    public BigDecimal getLocationPlan() {
        return this.locationPlan;
    }

    public BigDecimal getMaxDailyRevenue() {
        return this.maxDailyRevenue;
    }

    public Double getPlanCompletionPercent() {
        return this.planCompletionPercent;
    }

    public BigDecimal getAvgRevenuePerDay() {
        return this.avgRevenuePerDay;
    }

    public BigDecimal getActualIncome() {
        return this.actualIncome;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setDailyFigure(BigDecimal dailyFigure) {
        this.dailyFigure = dailyFigure;
    }

    public void setLocationRemainingToPlan(BigDecimal locationRemainingToPlan) {
        this.locationRemainingToPlan = locationRemainingToPlan;
    }

    public void setLocationPlan(BigDecimal locationPlan) {
        this.locationPlan = locationPlan;
    }

    public void setMaxDailyRevenue(BigDecimal maxDailyRevenue) {
        this.maxDailyRevenue = maxDailyRevenue;
    }

    public void setPlanCompletionPercent(Double planCompletionPercent) {
        this.planCompletionPercent = planCompletionPercent;
    }

    public void setAvgRevenuePerDay(BigDecimal avgRevenuePerDay) {
        this.avgRevenuePerDay = avgRevenuePerDay;
    }

    public void setActualIncome(BigDecimal actualIncome) {
        this.actualIncome = actualIncome;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof LocationKpi)) return false;
        final LocationKpi other = (LocationKpi) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$dailyFigure = this.getDailyFigure();
        final Object other$dailyFigure = other.getDailyFigure();
        if (this$dailyFigure == null ? other$dailyFigure != null : !this$dailyFigure.equals(other$dailyFigure))
            return false;
        final Object this$locationRemainingToPlan = this.getLocationRemainingToPlan();
        final Object other$locationRemainingToPlan = other.getLocationRemainingToPlan();
        if (this$locationRemainingToPlan == null ? other$locationRemainingToPlan != null : !this$locationRemainingToPlan.equals(other$locationRemainingToPlan))
            return false;
        final Object this$locationPlan = this.getLocationPlan();
        final Object other$locationPlan = other.getLocationPlan();
        if (this$locationPlan == null ? other$locationPlan != null : !this$locationPlan.equals(other$locationPlan))
            return false;
        final Object this$maxDailyRevenue = this.getMaxDailyRevenue();
        final Object other$maxDailyRevenue = other.getMaxDailyRevenue();
        if (this$maxDailyRevenue == null ? other$maxDailyRevenue != null : !this$maxDailyRevenue.equals(other$maxDailyRevenue))
            return false;
        final Object this$planCompletionPercent = this.getPlanCompletionPercent();
        final Object other$planCompletionPercent = other.getPlanCompletionPercent();
        if (this$planCompletionPercent == null ? other$planCompletionPercent != null : !this$planCompletionPercent.equals(other$planCompletionPercent))
            return false;
        final Object this$avgRevenuePerDay = this.getAvgRevenuePerDay();
        final Object other$avgRevenuePerDay = other.getAvgRevenuePerDay();
        if (this$avgRevenuePerDay == null ? other$avgRevenuePerDay != null : !this$avgRevenuePerDay.equals(other$avgRevenuePerDay))
            return false;
        final Object this$actualIncome = this.getActualIncome();
        final Object other$actualIncome = other.getActualIncome();
        if (this$actualIncome == null ? other$actualIncome != null : !this$actualIncome.equals(other$actualIncome))
            return false;
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof LocationKpi;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $dailyFigure = this.getDailyFigure();
        result = result * PRIME + ($dailyFigure == null ? 43 : $dailyFigure.hashCode());
        final Object $locationRemainingToPlan = this.getLocationRemainingToPlan();
        result = result * PRIME + ($locationRemainingToPlan == null ? 43 : $locationRemainingToPlan.hashCode());
        final Object $locationPlan = this.getLocationPlan();
        result = result * PRIME + ($locationPlan == null ? 43 : $locationPlan.hashCode());
        final Object $maxDailyRevenue = this.getMaxDailyRevenue();
        result = result * PRIME + ($maxDailyRevenue == null ? 43 : $maxDailyRevenue.hashCode());
        final Object $planCompletionPercent = this.getPlanCompletionPercent();
        result = result * PRIME + ($planCompletionPercent == null ? 43 : $planCompletionPercent.hashCode());
        final Object $avgRevenuePerDay = this.getAvgRevenuePerDay();
        result = result * PRIME + ($avgRevenuePerDay == null ? 43 : $avgRevenuePerDay.hashCode());
        final Object $actualIncome = this.getActualIncome();
        result = result * PRIME + ($actualIncome == null ? 43 : $actualIncome.hashCode());
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        return result;
    }

    public String toString() {
        return "LocationKpi(dailyFigure=" + this.getDailyFigure() + ", locationRemainingToPlan=" + this.getLocationRemainingToPlan() + ", locationPlan=" + this.getLocationPlan() + ", maxDailyRevenue=" + this.getMaxDailyRevenue() + ", planCompletionPercent=" + this.getPlanCompletionPercent() + ", avgRevenuePerDay=" + this.getAvgRevenuePerDay() + ", actualIncome=" + this.getActualIncome() + ", location=" + this.getLocation() + ")";
    }
}
