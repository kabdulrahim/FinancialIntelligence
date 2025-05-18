package com.fintech.wcm.repository;

import com.fintech.wcm.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for accessing and manipulating Payment entities.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payments for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of payments for the company
     */
    List<Payment> findByCompanyId(Long companyId);
    
    /**
     * Find payments for a specific invoice.
     * 
     * @param invoiceId the invoice ID
     * @return a list of payments
     */
    List<Payment> findByInvoiceId(Long invoiceId);
    
    /**
     * Find payments for a specific company with dates in a given range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of payments
     */
    List<Payment> findByCompanyIdAndPaymentDateBetween(
            Long companyId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find payments for a specific cash account.
     * 
     * @param cashAccountId the cash account ID
     * @return a list of payments
     */
    List<Payment> findByCashAccountId(Long cashAccountId);
    
    /**
     * Calculate the total payments received for a company within a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the total payments in base currency
     */
    @Query("SELECT SUM(p.amountBaseCurrency) FROM Payment p JOIN p.invoice i WHERE p.company.id = :companyId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate AND i.invoiceType = 'SALES'")
    BigDecimal sumPaymentsReceivedByCompanyIdAndDateRange(
            @Param("companyId") Long companyId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * Calculate the total payments made for a company within a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the total payments in base currency
     */
    @Query("SELECT SUM(p.amountBaseCurrency) FROM Payment p JOIN p.invoice i WHERE p.company.id = :companyId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate AND i.invoiceType = 'PURCHASE'")
    BigDecimal sumPaymentsMadeByCompanyIdAndDateRange(
            @Param("companyId") Long companyId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
