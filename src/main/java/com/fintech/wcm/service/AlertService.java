package com.fintech.wcm.service;

import com.fintech.wcm.model.Alert;

import java.util.List;

/**
 * Service interface for managing alerts.
 */
public interface AlertService {
    
    /**
     * Create a new alert.
     * 
     * @param alert the alert to create
     * @return the created alert
     */
    Alert createAlert(Alert alert);
    
    /**
     * Get all alerts for a company.
     * 
     * @param companyId the company ID
     * @return a list of alerts
     */
    List<Alert> getAlertsByCompanyId(Long companyId);
    
    /**
     * Get active (non-dismissed) alerts for a company.
     * 
     * @param companyId the company ID
     * @return a list of active alerts
     */
    List<Alert> getActiveAlertsByCompanyId(Long companyId);
    
    /**
     * Get unread alerts for a company.
     * 
     * @param companyId the company ID
     * @return a list of unread alerts
     */
    List<Alert> getUnreadAlertsByCompanyId(Long companyId);
    
    /**
     * Get alerts of a specific type for a company.
     * 
     * @param companyId the company ID
     * @param alertType the alert type
     * @return a list of alerts
     */
    List<Alert> getAlertsByCompanyIdAndType(Long companyId, Alert.AlertType alertType);
    
    /**
     * Get alerts with a specific severity for a company.
     * 
     * @param companyId the company ID
     * @param severity the alert severity
     * @return a list of alerts
     */
    List<Alert> getAlertsByCompanyIdAndSeverity(Long companyId, Alert.AlertSeverity severity);
    
    /**
     * Mark an alert as read.
     * 
     * @param alertId the alert ID
     */
    void markAlertAsRead(Long alertId);
    
    /**
     * Mark an alert as dismissed.
     * 
     * @param alertId the alert ID
     */
    void dismissAlert(Long alertId);
    
    /**
     * Generate cash gap alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    int generateCashGapAlerts(Long companyId);
    
    /**
     * Generate liquidity issue alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    int generateLiquidityAlerts(Long companyId);
    
    /**
     * Generate working capital ratio alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    int generateWorkingCapitalRatioAlerts(Long companyId);
    
    /**
     * Generate Cash Conversion Cycle (CCC) alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    int generateCCCAlerts(Long companyId);
}
