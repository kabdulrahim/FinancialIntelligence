package com.fintech.wcm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for the dashboard summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardSummaryDto {
    
    private Long companyId;
    private String companyName;
    private String companyType;
    private String currencyCode;
    private LocalDate asOfDate;
    
    // Key metrics
    private BigDecimal cashBalance;
    private BigDecimal accountsReceivable;
    private BigDecimal accountsPayable;
    private BigDecimal inventory;
    private BigDecimal netWorkingCapital;
    
    // Financial ratios
    private BigDecimal currentRatio;
    private BigDecimal quickRatio;
    private BigDecimal cashConversionCycle;
    private BigDecimal daysSalesOutstanding;
    private BigDecimal daysPayableOutstanding;
    private BigDecimal daysInventoryOutstanding;
    
    // Alerts
    private int totalAlerts;
    private int criticalAlerts;
    private int highAlerts;
    private List<Map<String, Object>> recentAlerts;
    
    // Cash flow
    private BigDecimal upcomingReceivables30Days;
    private BigDecimal upcomingPayables30Days;
    private BigDecimal projectedCashBalance30Days;
    
    // Working capital analysis
    private Map<String, Object> workingCapitalTrend;
    private Map<String, Object> topCustomers;
    private Map<String, Object> topVendors;
    
    // Recommended actions
    private List<Map<String, Object>> recommendations;
}
