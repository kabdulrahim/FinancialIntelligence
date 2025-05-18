package com.fintech.wcm.repository;

import com.fintech.wcm.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for accessing and manipulating Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transactions for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of transactions for the company
     */
    List<Transaction> findByCompanyId(Long companyId);
    
    /**
     * Find transactions for a specific company with dates in a given range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of transactions
     */
    List<Transaction> findByCompanyIdAndTransactionDateBetween(
            Long companyId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find transactions for a specific company with a specific transaction type.
     * 
     * @param companyId the company ID
     * @param transactionType the transaction type
     * @return a list of transactions
     */
    List<Transaction> findByCompanyIdAndTransactionType(
            Long companyId, Transaction.TransactionType transactionType);
    
    /**
     * Find transactions for a specific cash account.
     * 
     * @param cashAccountId the cash account ID
     * @return a list of transactions
     */
    List<Transaction> findByCashAccountId(Long cashAccountId);
    
    /**
     * Calculate the total income for a company within a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the total income in base currency
     */
    @Query("SELECT SUM(t.amountBaseCurrency) FROM Transaction t WHERE t.company.id = :companyId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate AND t.transactionType = 'INCOME'")
    BigDecimal sumIncomeByCompanyIdAndDateRange(
            @Param("companyId") Long companyId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * Calculate the total expenses for a company within a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the total expenses in base currency
     */
    @Query("SELECT SUM(t.amountBaseCurrency) FROM Transaction t WHERE t.company.id = :companyId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate AND t.transactionType = 'EXPENSE'")
    BigDecimal sumExpensesByCompanyIdAndDateRange(
            @Param("companyId") Long companyId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
