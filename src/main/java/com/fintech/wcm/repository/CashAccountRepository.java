package com.fintech.wcm.repository;

import com.fintech.wcm.model.CashAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for accessing and manipulating CashAccount entities.
 */
@Repository
public interface CashAccountRepository extends JpaRepository<CashAccount, Long> {
    
    /**
     * Find cash accounts for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of cash accounts for the company
     */
    List<CashAccount> findByCompanyId(Long companyId);
    
    /**
     * Find active cash accounts for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of active cash accounts
     */
    List<CashAccount> findByCompanyIdAndActiveTrue(Long companyId);
    
    /**
     * Find cash accounts for a specific company with a specific account type.
     * 
     * @param companyId the company ID
     * @param accountType the account type
     * @return a list of cash accounts
     */
    List<CashAccount> findByCompanyIdAndAccountType(Long companyId, CashAccount.AccountType accountType);
    
    /**
     * Calculate the total cash balance for a company across all accounts in base currency.
     * 
     * @param companyId the company ID
     * @return the total cash balance in base currency
     */
    @Query("SELECT SUM(ca.balanceBaseCurrency) FROM CashAccount ca WHERE ca.company.id = :companyId AND ca.active = true")
    BigDecimal sumTotalCashBalanceByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * Find a cash account by company ID and account number.
     * 
     * @param companyId the company ID
     * @param accountNumber the account number
     * @return a list of matching cash accounts
     */
    List<CashAccount> findByCompanyIdAndAccountNumber(Long companyId, String accountNumber);
}
