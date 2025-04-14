package com.levita.levita_monitoring.dto;

import java.math.BigDecimal;

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

    public DashboardDto(String username, String locationName, Double conversionRate, BigDecimal currentIncome, BigDecimal plannedIncome, BigDecimal mainSalaryPart, BigDecimal personalRevenue, BigDecimal dayBonuses, BigDecimal dailyFigure, BigDecimal remainingToPlan, BigDecimal locationPlan, BigDecimal maxDailyRevenue, Double planCompletionPercent, BigDecimal avgRevenuePerDay, BigDecimal actualIncome) {
        this.username = username;
        this.locationName = locationName;
        this.conversionRate = conversionRate;
        this.currentIncome = currentIncome;
        this.plannedIncome = plannedIncome;
        this.mainSalaryPart = mainSalaryPart;
        this.personalRevenue = personalRevenue;
        this.dayBonuses = dayBonuses;
        this.dailyFigure = dailyFigure;
        this.remainingToPlan = remainingToPlan;
        this.locationPlan = locationPlan;
        this.maxDailyRevenue = maxDailyRevenue;
        this.planCompletionPercent = planCompletionPercent;
        this.avgRevenuePerDay = avgRevenuePerDay;
        this.actualIncome = actualIncome;
    }

    public DashboardDto() {
    }

    public static DashboardDtoBuilder builder() {
        return new DashboardDtoBuilder();
    }

    public String getUsername() {
        return this.username;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public Double getConversionRate() {
        return this.conversionRate;
    }

    public BigDecimal getCurrentIncome() {
        return this.currentIncome;
    }

    public BigDecimal getPlannedIncome() {
        return this.plannedIncome;
    }

    public BigDecimal getMainSalaryPart() {
        return this.mainSalaryPart;
    }

    public BigDecimal getPersonalRevenue() {
        return this.personalRevenue;
    }

    public BigDecimal getDayBonuses() {
        return this.dayBonuses;
    }

    public BigDecimal getDailyFigure() {
        return this.dailyFigure;
    }

    public BigDecimal getRemainingToPlan() {
        return this.remainingToPlan;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public void setCurrentIncome(BigDecimal currentIncome) {
        this.currentIncome = currentIncome;
    }

    public void setPlannedIncome(BigDecimal plannedIncome) {
        this.plannedIncome = plannedIncome;
    }

    public void setMainSalaryPart(BigDecimal mainSalaryPart) {
        this.mainSalaryPart = mainSalaryPart;
    }

    public void setPersonalRevenue(BigDecimal personalRevenue) {
        this.personalRevenue = personalRevenue;
    }

    public void setDayBonuses(BigDecimal dayBonuses) {
        this.dayBonuses = dayBonuses;
    }

    public void setDailyFigure(BigDecimal dailyFigure) {
        this.dailyFigure = dailyFigure;
    }

    public void setRemainingToPlan(BigDecimal remainingToPlan) {
        this.remainingToPlan = remainingToPlan;
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DashboardDto)) return false;
        final DashboardDto other = (DashboardDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        final Object this$locationName = this.getLocationName();
        final Object other$locationName = other.getLocationName();
        if (this$locationName == null ? other$locationName != null : !this$locationName.equals(other$locationName))
            return false;
        final Object this$conversionRate = this.getConversionRate();
        final Object other$conversionRate = other.getConversionRate();
        if (this$conversionRate == null ? other$conversionRate != null : !this$conversionRate.equals(other$conversionRate))
            return false;
        final Object this$currentIncome = this.getCurrentIncome();
        final Object other$currentIncome = other.getCurrentIncome();
        if (this$currentIncome == null ? other$currentIncome != null : !this$currentIncome.equals(other$currentIncome))
            return false;
        final Object this$plannedIncome = this.getPlannedIncome();
        final Object other$plannedIncome = other.getPlannedIncome();
        if (this$plannedIncome == null ? other$plannedIncome != null : !this$plannedIncome.equals(other$plannedIncome))
            return false;
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
        final Object this$dailyFigure = this.getDailyFigure();
        final Object other$dailyFigure = other.getDailyFigure();
        if (this$dailyFigure == null ? other$dailyFigure != null : !this$dailyFigure.equals(other$dailyFigure))
            return false;
        final Object this$remainingToPlan = this.getRemainingToPlan();
        final Object other$remainingToPlan = other.getRemainingToPlan();
        if (this$remainingToPlan == null ? other$remainingToPlan != null : !this$remainingToPlan.equals(other$remainingToPlan))
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
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DashboardDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        final Object $locationName = this.getLocationName();
        result = result * PRIME + ($locationName == null ? 43 : $locationName.hashCode());
        final Object $conversionRate = this.getConversionRate();
        result = result * PRIME + ($conversionRate == null ? 43 : $conversionRate.hashCode());
        final Object $currentIncome = this.getCurrentIncome();
        result = result * PRIME + ($currentIncome == null ? 43 : $currentIncome.hashCode());
        final Object $plannedIncome = this.getPlannedIncome();
        result = result * PRIME + ($plannedIncome == null ? 43 : $plannedIncome.hashCode());
        final Object $mainSalaryPart = this.getMainSalaryPart();
        result = result * PRIME + ($mainSalaryPart == null ? 43 : $mainSalaryPart.hashCode());
        final Object $personalRevenue = this.getPersonalRevenue();
        result = result * PRIME + ($personalRevenue == null ? 43 : $personalRevenue.hashCode());
        final Object $dayBonuses = this.getDayBonuses();
        result = result * PRIME + ($dayBonuses == null ? 43 : $dayBonuses.hashCode());
        final Object $dailyFigure = this.getDailyFigure();
        result = result * PRIME + ($dailyFigure == null ? 43 : $dailyFigure.hashCode());
        final Object $remainingToPlan = this.getRemainingToPlan();
        result = result * PRIME + ($remainingToPlan == null ? 43 : $remainingToPlan.hashCode());
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
        return result;
    }

    public String toString() {
        return "DashboardDto(username=" + this.getUsername() + ", locationName=" + this.getLocationName() + ", conversionRate=" + this.getConversionRate() + ", currentIncome=" + this.getCurrentIncome() + ", plannedIncome=" + this.getPlannedIncome() + ", mainSalaryPart=" + this.getMainSalaryPart() + ", personalRevenue=" + this.getPersonalRevenue() + ", dayBonuses=" + this.getDayBonuses() + ", dailyFigure=" + this.getDailyFigure() + ", remainingToPlan=" + this.getRemainingToPlan() + ", locationPlan=" + this.getLocationPlan() + ", maxDailyRevenue=" + this.getMaxDailyRevenue() + ", planCompletionPercent=" + this.getPlanCompletionPercent() + ", avgRevenuePerDay=" + this.getAvgRevenuePerDay() + ", actualIncome=" + this.getActualIncome() + ")";
    }

    public static class DashboardDtoBuilder {
        private String username;
        private String locationName;
        private Double conversionRate;
        private BigDecimal currentIncome;
        private BigDecimal plannedIncome;
        private BigDecimal mainSalaryPart;
        private BigDecimal personalRevenue;
        private BigDecimal dayBonuses;
        private BigDecimal dailyFigure;
        private BigDecimal remainingToPlan;
        private BigDecimal locationPlan;
        private BigDecimal maxDailyRevenue;
        private Double planCompletionPercent;
        private BigDecimal avgRevenuePerDay;
        private BigDecimal actualIncome;

        DashboardDtoBuilder() {
        }

        public DashboardDtoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public DashboardDtoBuilder locationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        public DashboardDtoBuilder conversionRate(Double conversionRate) {
            this.conversionRate = conversionRate;
            return this;
        }

        public DashboardDtoBuilder currentIncome(BigDecimal currentIncome) {
            this.currentIncome = currentIncome;
            return this;
        }

        public DashboardDtoBuilder plannedIncome(BigDecimal plannedIncome) {
            this.plannedIncome = plannedIncome;
            return this;
        }

        public DashboardDtoBuilder mainSalaryPart(BigDecimal mainSalaryPart) {
            this.mainSalaryPart = mainSalaryPart;
            return this;
        }

        public DashboardDtoBuilder personalRevenue(BigDecimal personalRevenue) {
            this.personalRevenue = personalRevenue;
            return this;
        }

        public DashboardDtoBuilder dayBonuses(BigDecimal dayBonuses) {
            this.dayBonuses = dayBonuses;
            return this;
        }

        public DashboardDtoBuilder dailyFigure(BigDecimal dailyFigure) {
            this.dailyFigure = dailyFigure;
            return this;
        }

        public DashboardDtoBuilder remainingToPlan(BigDecimal remainingToPlan) {
            this.remainingToPlan = remainingToPlan;
            return this;
        }

        public DashboardDtoBuilder locationPlan(BigDecimal locationPlan) {
            this.locationPlan = locationPlan;
            return this;
        }

        public DashboardDtoBuilder maxDailyRevenue(BigDecimal maxDailyRevenue) {
            this.maxDailyRevenue = maxDailyRevenue;
            return this;
        }

        public DashboardDtoBuilder planCompletionPercent(Double planCompletionPercent) {
            this.planCompletionPercent = planCompletionPercent;
            return this;
        }

        public DashboardDtoBuilder avgRevenuePerDay(BigDecimal avgRevenuePerDay) {
            this.avgRevenuePerDay = avgRevenuePerDay;
            return this;
        }

        public DashboardDtoBuilder actualIncome(BigDecimal actualIncome) {
            this.actualIncome = actualIncome;
            return this;
        }

        public DashboardDto build() {
            return new DashboardDto(this.username, this.locationName, this.conversionRate, this.currentIncome, this.plannedIncome, this.mainSalaryPart, this.personalRevenue, this.dayBonuses, this.dailyFigure, this.remainingToPlan, this.locationPlan, this.maxDailyRevenue, this.planCompletionPercent, this.avgRevenuePerDay, this.actualIncome);
        }

        public String toString() {
            return "DashboardDto.DashboardDtoBuilder(username=" + this.username + ", locationName=" + this.locationName + ", conversionRate=" + this.conversionRate + ", currentIncome=" + this.currentIncome + ", plannedIncome=" + this.plannedIncome + ", mainSalaryPart=" + this.mainSalaryPart + ", personalRevenue=" + this.personalRevenue + ", dayBonuses=" + this.dayBonuses + ", dailyFigure=" + this.dailyFigure + ", remainingToPlan=" + this.remainingToPlan + ", locationPlan=" + this.locationPlan + ", maxDailyRevenue=" + this.maxDailyRevenue + ", planCompletionPercent=" + this.planCompletionPercent + ", avgRevenuePerDay=" + this.avgRevenuePerDay + ", actualIncome=" + this.actualIncome + ")";
        }
    }
}
