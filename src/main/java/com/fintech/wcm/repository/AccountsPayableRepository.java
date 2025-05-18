package com.fintech.wcm.repository;

import com.fintech.wcm.model.AccountsPayable;
import com.fintech.wcm.model.AccountsPayable.PayableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for accessing and manipulating AccountsPayable entities.
 */
@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {
    
    /**
     * Find accounts payable for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of accounts payable for the company
     */
    List<AccountsPayable> findByCompanyId(Long companyId);
    
    /**
     * Find accounts payable for a specific company with a specific status.
     * 
     * @param companyId the company ID
     * @param status the payable status
     * @return a list of accounts payable
     */
    List<AccountsPayable> findByCompanyIdAndStatus(Long companyId, PayableStatus status);
    
    /**
     * Find due accounts payable for a specific company.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of due accounts payable in the date range
     */
    List<AccountsPayable> findByCompanyIdAndDueDateBetweenAndStatusIn(
            Long companyId, LocalDate startDate, LocalDate endDate, List<PayableStatus> statuses);
    
    /**
     * Calculate the total amount of accounts payable for a company.
     * 
     * @param companyId the company ID
     * @return the total amount in base currency
     */
    @Query("SELECT SUM(ap.amountBaseCurrency) FROM AccountsPayable ap WHERE ap.company.id = :companyId " +
           "AND ap.status IN ('PENDING', 'APPROVED', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal sumTotalPayablesByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * Calculate the total amount of overdue accounts payable for a company.
     * 
     * @param companyId the company ID
     * @param currentDate the current date
     * @return the total overdue amount in base currency
     */
    @Query("SELECT SUM(ap.amountBaseCurrency) FROM AccountsPayable ap WHERE ap.company.id = :companyId " +
           "AND ap.dueDate < :currentDate AND ap.status IN ('PENDING', 'APPROVED', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal sumOverduePayablesByCompanyId(
            @Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Find payables grouped by vendor for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of accounts payable
     */
    @Query("SELECT ap.vendorName, SUM(ap.amountBaseCurrency) as total FROM AccountsPayable ap " +
           "WHERE ap.company.id = :companyId AND ap.status IN ('PENDING', 'APPROVED', 'PARTIALLY_PAID', 'OVERDUE') " +
           "GROUP BY ap.vendorName ORDER BY total DESC")
    List<Object[]> findPayablesByVendor(@Param("companyId") Long companyId);
}
