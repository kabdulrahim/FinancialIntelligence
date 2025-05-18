package com.fintech.wcm.service;

import com.fintech.wcm.dto.CompanyDto;

import java.util.List;

/**
 * Service interface for managing company-related operations.
 */
public interface CompanyService {
    
    /**
     * Create a new company.
     * 
     * @param companyDto the company data
     * @return the created company
     */
    CompanyDto createCompany(CompanyDto companyDto);
    
    /**
     * Update an existing company.
     * 
     * @param id the company ID
     * @param companyDto the company data
     * @return the updated company
     */
    CompanyDto updateCompany(Long id, CompanyDto companyDto);
    
    /**
     * Get a company by ID.
     * 
     * @param id the company ID
     * @return the company
     */
    CompanyDto getCompanyById(Long id);
    
    /**
     * Get a company by name.
     * 
     * @param name the company name
     * @return the company
     */
    CompanyDto getCompanyByName(String name);
    
    /**
     * Get all companies.
     * 
     * @return a list of companies
     */
    List<CompanyDto> getAllCompanies();
    
    /**
     * Get companies by type.
     * 
     * @param type the company type (SME or MNE)
     * @return a list of companies
     */
    List<CompanyDto> getCompaniesByType(String type);
    
    /**
     * Deactivate a company.
     * 
     * @param id the company ID
     */
    void deactivateCompany(Long id);
    
    /**
     * Activate a company.
     * 
     * @param id the company ID
     */
    void activateCompany(Long id);
    
    /**
     * Check if a company name is available.
     * 
     * @param name the company name
     * @return true if the name is available
     */
    boolean isCompanyNameAvailable(String name);
    
    /**
     * Check if a company tax ID is available.
     * 
     * @param taxId the tax ID
     * @return true if the tax ID is available
     */
    boolean isCompanyTaxIdAvailable(String taxId);
}
