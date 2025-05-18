package com.fintech.wcm.controller;

import com.fintech.wcm.dto.CompanyDto;
import com.fintech.wcm.dto.UserDto;
import com.fintech.wcm.service.CompanyService;
import com.fintech.wcm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for company-related endpoints.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Companies", description = "Company Management API")
public class CompanyController {

    private final CompanyService companyService;
    private final UserService userService;

    /**
     * Endpoint to create a new company.
     * 
     * @param companyDto the company data
     * @return the created company
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new company", description = "Creates a new company (Admin only)")
    public ResponseEntity<CompanyDto> createCompany(@RequestBody CompanyDto companyDto) {
        CompanyDto createdCompany = companyService.createCompany(companyDto);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }

    /**
     * Endpoint to update an existing company.
     * 
     * @param id the company ID
     * @param companyDto the company data
     * @return the updated company
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    @Operation(summary = "Update a company", description = "Updates an existing company (Admin or Owner only)")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @RequestBody CompanyDto companyDto) {
        CompanyDto updatedCompany = companyService.updateCompany(id, companyDto);
        return ResponseEntity.ok(updatedCompany);
    }

    /**
     * Endpoint to get a company by ID.
     * 
     * @param id the company ID
     * @return the company
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #id)")
    @Operation(summary = "Get a company by ID", description = "Retrieves a company by its ID")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        CompanyDto company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    /**
     * Endpoint to get all companies.
     * 
     * @return a list of companies
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all companies", description = "Retrieves all companies (Admin only)")
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        List<CompanyDto> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    /**
     * Endpoint to get companies by type.
     * 
     * @param type the company type (SME or MNE)
     * @return a list of companies
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get companies by type", description = "Retrieves companies by their type (SME or MNE) (Admin only)")
    public ResponseEntity<List<CompanyDto>> getCompaniesByType(@PathVariable String type) {
        List<CompanyDto> companies = companyService.getCompaniesByType(type);
        return ResponseEntity.ok(companies);
    }

    /**
     * Endpoint to deactivate a company.
     * 
     * @param id the company ID
     * @return a response with no content
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a company", description = "Deactivates a company (Admin only)")
    public ResponseEntity<Void> deactivateCompany(@PathVariable Long id) {
        companyService.deactivateCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to activate a company.
     * 
     * @param id the company ID
     * @return a response with no content
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a company", description = "Activates a company (Admin only)")
    public ResponseEntity<Void> activateCompany(@PathVariable Long id) {
        companyService.activateCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to get users for a company.
     * 
     * @param id the company ID
     * @return a list of users
     */
    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #id)")
    @Operation(summary = "Get users for a company", description = "Retrieves users belonging to a company")
    public ResponseEntity<List<UserDto>> getCompanyUsers(@PathVariable Long id) {
        List<UserDto> users = userService.getUsersByCompanyId(id);
        return ResponseEntity.ok(users);
    }
}
