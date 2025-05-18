package com.fintech.wcm.repository;

import com.fintech.wcm.model.ShortTermLiability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for accessing and manipulating ShortTermLiability entities.
 */
@Repository
public interface ShortTermLiabilityRepository extends JpaRepository<ShortTermLiability, Long> {
    
    /**
     * Find short-term liabilities for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of short-term liabilities for the company
     */
    List<ShortTermLiability> findByCompanyId(Long companyId);
    
    /**
     * Find active short-term liabilities for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of active short-term liabilities
     */
    List<ShortTermLiability> findByCompanyIdAndStatus(Long companyId, ShortTermLiability.LiabilityStatus status);
    
    /**
     * Find due short-term liabilities for a specific company within a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of due short-term liabilities in the date range
     */
    List<ShortTermLiability> findByCompanyIdAndDueDateBetweenAndStatusIn(
            Long companyId, LocalDate startDate, LocalDate endDate, List<ShortTermLiability.LiabilityStatus> statuses);
    
    /**
     * Calculate the total amount of short-term liabilities for a company.
     * 
     * @param companyId the company ID
     * @return the total amount in base currency
     */
    @Query("SELECT SUM(stl.amountBaseCurrency) FROM ShortTermLiability stl WHERE stl.company.id = :companyId " +
           "AND stl.status = 'ACTIVE'")
    BigDecimal sumTotalLiabilitiesByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * Calculate the total amount of short-term liabilities by type for a company.
     * 
     * @param companyId the company ID
     * @return a list of total amounts by liability type
     */
    @Query("SELECT stl.liabilityType, SUM(stl.amountBaseCurrency) FROM ShortTermLiability stl " +
           "WHERE stl.company.id = :companyId AND stl.status = 'ACTIVE' " +
           "GROUP BY stl.liabilityType ORDER BY SUM(stl.amountBaseCurrency) DESC")
    List<Object[]> sumLiabilitiesByType(@Param("companyId") Long companyId);
}
