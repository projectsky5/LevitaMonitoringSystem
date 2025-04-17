package com.levita.levita_monitoring.dto;

import com.levita.levita_monitoring.model.User;

import java.util.Objects;

public class AdminDto {
    private Long id;
    private String nameWithLocation;

    public AdminDto(User user) {
        this.id = user.getId();
        this.nameWithLocation = user.getName() + " - " + user.getLocation().getName();
    }

    public Long getId() {
        return id;
    }

    public String getNameWithLocation() {
        return nameWithLocation;
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
