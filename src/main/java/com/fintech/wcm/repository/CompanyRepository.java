package com.fintech.wcm.repository;

import com.fintech.wcm.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and manipulating Company entities.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Find a company by its name.
     * 
     * @param name the company name to search for
     * @return an Optional containing the company if found
     */
    Optional<Company> findByName(String name);
    
    /**
     * Find a company by its tax ID.
     * 
     * @param taxId the tax ID to search for
     * @return an Optional containing the company if found
     */
    Optional<Company> findByTaxId(String taxId);
    
    /**
     * Find companies by their type (SME or MNE).
     * 
     * @param type the company type to filter by
     * @return a list of companies of the specified type
     */
    List<Company> findByType(Company.CompanyType type);
    
    /**
     * Find active companies.
     * 
     * @return a list of active companies
     */
    List<Company> findByActiveTrue();
    
    /**
     * Check if a company with the given name already exists.
     * 
     * @param name the company name to check
     * @return true if a company with the name exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Check if a company with the given tax ID already exists.
     * 
     * @param taxId the tax ID to check
     * @return true if a company with the tax ID exists, false otherwise
     */
    boolean existsByTaxId(String taxId);
}
