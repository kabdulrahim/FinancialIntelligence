package com.fintech.wcm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Company entities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDto {
    
    private Long id;
    private String name;
    private String type;  // SME or MNE
    private String taxId;
    private String registrationNumber;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String phone;
    private String website;
    private String industry;
    private String fiscalYearEnd;
    private String currencyCode;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
