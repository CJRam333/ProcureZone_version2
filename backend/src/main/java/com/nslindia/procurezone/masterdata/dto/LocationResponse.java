package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.nslindia.procurezone.masterdata.Location;

/**
 * Response DTO for Location entity.
 */
public record LocationResponse(
        Integer id,
        String code,
        String name,
        Integer status,
        String statusName,
        LocalDate lastModifiedDate,
        Integer lastModifiedBy) {

    /**
     * Maps Location entity to LocationResponse DTO.
     */
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getStatus(),
                location.isActive() ? "Active" : "Inactive",
                location.getLastModifiedDate(),
                location.getLastModifiedUser());
    }
}
