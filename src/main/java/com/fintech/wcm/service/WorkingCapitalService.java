package com.fintech.wcm.service;

import com.fintech.wcm.dto.DashboardSummaryDto;
import com.fintech.wcm.dto.WorkingCapitalMetricsDto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service interface for working capital management operations.
 */
public interface WorkingCapitalService {
    
    /**
     * Calculate working capital metrics for a company.
     * 
     * @param companyId the company ID
     * @return the working capital metrics
     */
    WorkingCapitalMetricsDto calculateWorkingCapitalMetrics(Long companyId);
    
    /**
     * Calculate working capital metrics for a company as of a specific date.
     * 
     * @param companyId the company ID
     * @param asOfDate the date for which to calculate metrics
     * @return the working capital metrics
     */
    WorkingCapitalMetricsDto calculateWorkingCapitalMetrics(Long companyId, LocalDate asOfDate);
    
    /**
     * Get dashboard summary for a company.
     * 
     * @param companyId the company ID
     * @return the dashboard summary
     */
    DashboardSummaryDto getDashboardSummary(Long companyId);
    
    /**
     * Calculate the Days Sales Outstanding (DSO) for a company.
     * 
     * @param companyId the company ID
     * @return the DSO value
     */
    double calculateDSO(Long companyId);
    
    /**
     * Calculate the Days Payable Outstanding (DPO) for a company.
     * 
     * @param companyId the company ID
     * @return the DPO value
     */
    double calculateDPO(Long companyId);
    
    /**
     * Calculate the Days Inventory Outstanding (DIO) for a company.
     * 
     * @param companyId the company ID
     * @return the DIO value
     */
    double calculateDIO(Long companyId);
    
    /**
     * Calculate the Cash Conversion Cycle (CCC) for a company.
     * 
     * @param companyId the company ID
     * @return the CCC value
     */
    double calculateCCC(Long companyId);
    
    /**
     * Calculate liquidity ratios for a company.
     * 
     * @param companyId the company ID
     * @return a map of ratio names to values
     */
    Map<String, Double> calculateLiquidityRatios(Long companyId);
    
    /**
     * Get historical working capital metrics for a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @param interval the interval (DAILY, WEEKLY, MONTHLY)
     * @return a map of dates to metrics
     */
    Map<LocalDate, WorkingCapitalMetricsDto> getHistoricalMetrics(
            Long companyId, LocalDate startDate, LocalDate endDate, String interval);
    
    /**
     * Generate alerts for cash gaps or liquidity issues.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    int generateAlerts(Long companyId);
}
