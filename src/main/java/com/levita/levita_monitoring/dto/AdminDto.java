package com.levita.levita_monitoring.dto;

import com.levita.levita_monitoring.model.User;

import java.math.BigDecimal;
import java.util.Objects;

public class AdminDto {
    private Long id;
    private String nameWithLocation;
    private Double conversionRate;
    private BigDecimal personalRevenue;

    public AdminDto(User user) {
        this.id = user.getId();
        this.nameWithLocation = user.getName() + " - " + user.getLocation().getName();

        if(user.getUserKpi() != null) {
            this.conversionRate = user.getUserKpi().getConversionRate();
            this.personalRevenue = user.getUserKpi().getPersonalRevenue();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNameWithLocation() {
        return nameWithLocation;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public BigDecimal getPersonalRevenue() {
        return personalRevenue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminDto adminDto)) return false;
        return Objects.equals(id, adminDto.id) && Objects.equals(nameWithLocation, adminDto.nameWithLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameWithLocation);
    }

}
