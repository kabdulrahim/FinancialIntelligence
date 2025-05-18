package com.fintech.wcm.repository;

import com.fintech.wcm.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and manipulating Invoice entities.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Find invoices for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of invoices for the company
     */
    List<Invoice> findByCompanyId(Long companyId);
    
    /**
     * Find invoices for a specific company with a specific type.
     * 
     * @param companyId the company ID
     * @param invoiceType the invoice type
     * @return a list of invoices
     */
    List<Invoice> findByCompanyIdAndInvoiceType(Long companyId, Invoice.InvoiceType invoiceType);
    
    /**
     * Find invoices for a specific company with a specific status.
     * 
     * @param companyId the company ID
     * @param status the invoice status
     * @return a list of invoices
     */
    List<Invoice> findByCompanyIdAndStatus(Long companyId, Invoice.InvoiceStatus status);
    
    /**
     * Find invoices for a specific company with due dates in a given range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of invoices
     */
    List<Invoice> findByCompanyIdAndDueDateBetween(Long companyId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find an invoice by company ID and invoice number.
     * 
     * @param companyId the company ID
     * @param invoiceNumber the invoice number
     * @return an Optional containing the invoice if found
     */
    Optional<Invoice> findByCompanyIdAndInvoiceNumber(Long companyId, String invoiceNumber);
    
    /**
     * Calculate the total amount of open sales invoices for a company.
     * 
     * @param companyId the company ID
     * @return the total amount in base currency
     */
    @Query("SELECT SUM(i.totalAmountBaseCurrency) FROM Invoice i WHERE i.company.id = :companyId " +
           "AND i.invoiceType = 'SALES' AND i.status IN ('SENT', 'OVERDUE', 'PARTIALLY_PAID')")
    BigDecimal sumTotalOpenSalesInvoicesByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * Calculate the total amount of open purchase invoices for a company.
     * 
     * @param companyId the company ID
     * @return the total amount in base currency
     */
    @Query("SELECT SUM(i.totalAmountBaseCurrency) FROM Invoice i WHERE i.company.id = :companyId " +
           "AND i.invoiceType = 'PURCHASE' AND i.status IN ('PENDING', 'APPROVED', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal sumTotalOpenPurchaseInvoicesByCompanyId(@Param("companyId") Long companyId);
}
