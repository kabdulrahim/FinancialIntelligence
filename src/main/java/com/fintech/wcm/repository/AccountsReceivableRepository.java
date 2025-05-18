package com.fintech.wcm.repository;

import com.fintech.wcm.model.AccountsReceivable;
import com.fintech.wcm.model.AccountsReceivable.ReceivableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for accessing and manipulating AccountsReceivable entities.
 */
@Repository
public interface AccountsReceivableRepository extends JpaRepository<AccountsReceivable, Long> {
    
    /**
     * Find accounts receivable for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of accounts receivable for the company
     */
    List<AccountsReceivable> findByCompanyId(Long companyId);
    
    /**
     * Find accounts receivable for a specific company with a specific status.
     * 
     * @param companyId the company ID
     * @param status the receivable status
     * @return a list of accounts receivable
     */
    List<AccountsReceivable> findByCompanyIdAndStatus(Long companyId, ReceivableStatus status);
    
    /**
     * Find overdue accounts receivable for a specific company.
     * 
     * @param companyId the company ID
     * @param currentDate the current date to compare against due dates
     * @return a list of overdue accounts receivable
     */
    List<AccountsReceivable> findByCompanyIdAndDueDateBeforeAndStatusNot(
            Long companyId, LocalDate currentDate, ReceivableStatus status);
    
    /**
     * Calculate the total amount of accounts receivable for a company.
     * 
     * @param companyId the company ID
     * @return the total amount in base currency
     */
    @Query("SELECT SUM(ar.amountBaseCurrency) FROM AccountsReceivable ar WHERE ar.company.id = :companyId " +
           "AND ar.status IN ('OPEN', 'OVERDUE', 'PARTIALLY_PAID', 'DISPUTED')")
    BigDecimal sumTotalReceivablesByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * Calculate the total amount of overdue accounts receivable for a company.
     * 
     * @param companyId the company ID
     * @param currentDate the current date
     * @return the total overdue amount in base currency
     */
    @Query("SELECT SUM(ar.amountBaseCurrency) FROM AccountsReceivable ar WHERE ar.company.id = :companyId " +
           "AND ar.dueDate < :currentDate AND ar.status IN ('OPEN', 'OVERDUE', 'PARTIALLY_PAID')")
    BigDecimal sumOverdueReceivablesByCompanyId(
            @Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Find receivables grouped by customer for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of accounts receivable
     */
    @Query("SELECT ar.customerName, SUM(ar.amountBaseCurrency) as total FROM AccountsReceivable ar " +
           "WHERE ar.company.id = :companyId AND ar.status IN ('OPEN', 'OVERDUE', 'PARTIALLY_PAID', 'DISPUTED') " +
           "GROUP BY ar.customerName ORDER BY total DESC")
    List<Object[]> findReceivablesByCustomer(@Param("companyId") Long companyId);
}
