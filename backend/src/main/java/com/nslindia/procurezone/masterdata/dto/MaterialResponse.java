package com.nslindia.procurezone.masterdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialResponse(
        @JsonProperty("id")               Integer    id,
        @JsonProperty("materialCode")     String     materialCode,
        @JsonProperty("materialName")     String     materialName,
        @JsonProperty("description")      String     description,
        @JsonProperty("status")           Integer    status,
        @JsonProperty("statusText")       String     statusText,
        @JsonProperty("isActive")         Boolean    isActive,
        @JsonProperty("stockQuantity")    BigDecimal stockQuantity,
        @JsonProperty("companyName")      String     companyName,
        @JsonProperty("plantName")        String     plantName,
        @JsonProperty("lastModifiedDate") LocalDate  lastModifiedDate,
        @JsonProperty("lastModifiedBy")   Integer    lastModifiedBy) {
}
