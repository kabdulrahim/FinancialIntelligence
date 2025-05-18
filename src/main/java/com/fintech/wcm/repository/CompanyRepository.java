package com.fintech.wcm.repository;

import com.fintech.wcm.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Company entity.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Find a company by its name.
     * 
     * @param name the company name
     * @return the company if found
     */
    Optional<Company> findByName(String name);
    
    /**
     * Check if a company exists by name.
     * 
     * @param name the company name
     * @return true if exists
     */
    boolean existsByName(String name);
}