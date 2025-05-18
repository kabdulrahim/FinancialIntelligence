package com.fintech.wcm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Working Capital metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkingCapitalMetricsDto {
    
    private Long companyId;
    private String companyName;
    private LocalDate calculationDate;
    
    // Current Assets
    private BigDecimal totalCurrentAssets;
    private BigDecimal cashAndEquivalents;
    private BigDecimal accountsReceivable;
    private BigDecimal inventory;
    private BigDecimal otherCurrentAssets;
    
    // Current Liabilities
    private BigDecimal totalCurrentLiabilities;
    private BigDecimal accountsPayable;
    private BigDecimal shortTermDebt;
    private BigDecimal otherCurrentLiabilities;
    
    // Working Capital Metrics
    private BigDecimal netWorkingCapital;
    private BigDecimal currentRatio;
    private BigDecimal quickRatio;
    private BigDecimal workingCapitalTurnover;
    
    // Cash Conversion Cycle (CCC) Components
    private BigDecimal daysInventoryOutstanding; // DIO
    private BigDecimal daysSalesOutstanding;     // DSO
    private BigDecimal daysPayablesOutstanding;  // DPO
    private BigDecimal cashConversionCycle;      // CCC = DIO + DSO - DPO
    
    // Liquidity Metrics
    private BigDecimal cashRatio;
    private BigDecimal operatingCashFlow;
    private BigDecimal operatingCashFlowRatio;
    
    // Trend Indicators (comparing to previous periods)
    private String workingCapitalTrend;      // IMPROVING, STABLE, DETERIORATING
    private String cashConversionCycleTrend; // IMPROVING, STABLE, DETERIORATING
    private String liquidityTrend;           // IMPROVING, STABLE, DETERIORATING
}
