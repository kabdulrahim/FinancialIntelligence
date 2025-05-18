package com.fintech.wcm.repository;

import com.fintech.wcm.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing and manipulating Alert entities.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    /**
     * Find alerts for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of alerts for the company
     */
    List<Alert> findByCompanyId(Long companyId);
    
    /**
     * Find active (non-dismissed) alerts for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of active alerts
     */
    List<Alert> findByCompanyIdAndDismissedFalse(Long companyId);
    
    /**
     * Find unread alerts for a specific company.
     * 
     * @param companyId the company ID
     * @return a list of unread alerts
     */
    List<Alert> findByCompanyIdAndReadFalse(Long companyId);
    
    /**
     * Find alerts of a specific type for a company.
     * 
     * @param companyId the company ID
     * @param alertType the alert type
     * @return a list of alerts
     */
    List<Alert> findByCompanyIdAndAlertType(Long companyId, Alert.AlertType alertType);
    
    /**
     * Find alerts with a specific severity for a company.
     * 
     * @param companyId the company ID
     * @param severity the alert severity
     * @return a list of alerts
     */
    List<Alert> findByCompanyIdAndSeverity(Long companyId, Alert.AlertSeverity severity);
    
    /**
     * Count unread alerts for a specific company.
     * 
     * @param companyId the company ID
     * @return the count of unread alerts
     */
    long countByCompanyIdAndReadFalse(Long companyId);
}
