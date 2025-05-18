package com.fintech.wcm.repository;

import com.fintech.wcm.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Currency entity.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    
    /**
     * Find a currency by its code.
     * 
     * @param code the currency code
     * @return the currency if found
     */
    Optional<Currency> findByCode(String code);
    
    /**
     * Find all active currencies.
     * 
     * @return list of active currencies
     */
    List<Currency> findByIsActiveTrue();
    
    /**
     * Find the base currency.
     * 
     * @return the base currency if found
     */
    Optional<Currency> findByIsBaseCurrencyTrue();
}