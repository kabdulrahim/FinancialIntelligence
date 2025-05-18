package com.fintech.wcm.controller;

import com.fintech.wcm.dto.WorkingCapitalMetricsDto;
import com.fintech.wcm.exception.BadRequestException;
import com.fintech.wcm.service.WorkingCapitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for working capital-related endpoints.
 */
@RestController
@RequestMapping("/api/working-capital")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Working Capital", description = "Working Capital Management API")
public class WorkingCapitalController {

    private final WorkingCapitalService workingCapitalService;

    /**
     * Endpoint to calculate working capital metrics for a company.
     * 
     * @param companyId the company ID
     * @return the working capital metrics
     */
    @GetMapping("/metrics/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate working capital metrics", description = "Calculates working capital metrics for a company")
    public ResponseEntity<WorkingCapitalMetricsDto> calculateWorkingCapitalMetrics(@PathVariable Long companyId) {
        WorkingCapitalMetricsDto metrics = workingCapitalService.calculateWorkingCapitalMetrics(companyId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Endpoint to calculate working capital metrics for a company as of a specific date.
     * 
     * @param companyId the company ID
     * @param asOfDate the date for which to calculate metrics
     * @return the working capital metrics
     */
    @GetMapping("/metrics/{companyId}/as-of/{asOfDate}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate working capital metrics as of a date", description = "Calculates working capital metrics for a company as of a specific date")
    public ResponseEntity<WorkingCapitalMetricsDto> calculateWorkingCapitalMetricsAsOf(
            @PathVariable Long companyId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        WorkingCapitalMetricsDto metrics = workingCapitalService.calculateWorkingCapitalMetrics(companyId, asOfDate);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Endpoint to calculate Days Sales Outstanding (DSO) for a company.
     * 
     * @param companyId the company ID
     * @return the DSO value
     */
    @GetMapping("/dso/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate DSO", description = "Calculates Days Sales Outstanding for a company")
    public ResponseEntity<Map<String, Double>> calculateDSO(@PathVariable Long companyId) {
        double dso = workingCapitalService.calculateDSO(companyId);
        return ResponseEntity.ok(Map.of("dso", dso));
    }

    /**
     * Endpoint to calculate Days Payable Outstanding (DPO) for a company.
     * 
     * @param companyId the company ID
     * @return the DPO value
     */
    @GetMapping("/dpo/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate DPO", description = "Calculates Days Payable Outstanding for a company")
    public ResponseEntity<Map<String, Double>> calculateDPO(@PathVariable Long companyId) {
        double dpo = workingCapitalService.calculateDPO(companyId);
        return ResponseEntity.ok(Map.of("dpo", dpo));
    }

    /**
     * Endpoint to calculate Days Inventory Outstanding (DIO) for a company.
     * 
     * @param companyId the company ID
     * @return the DIO value
     */
    @GetMapping("/dio/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate DIO", description = "Calculates Days Inventory Outstanding for a company")
    public ResponseEntity<Map<String, Double>> calculateDIO(@PathVariable Long companyId) {
        double dio = workingCapitalService.calculateDIO(companyId);
        return ResponseEntity.ok(Map.of("dio", dio));
    }

    /**
     * Endpoint to calculate Cash Conversion Cycle (CCC) for a company.
     * 
     * @param companyId the company ID
     * @return the CCC value
     */
    @GetMapping("/ccc/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate CCC", description = "Calculates Cash Conversion Cycle for a company")
    public ResponseEntity<Map<String, Double>> calculateCCC(@PathVariable Long companyId) {
        double ccc = workingCapitalService.calculateCCC(companyId);
        return ResponseEntity.ok(Map.of("ccc", ccc));
    }

    /**
     * Endpoint to calculate liquidity ratios for a company.
     * 
     * @param companyId the company ID
     * @return a map of ratio names to values
     */
    @GetMapping("/liquidity-ratios/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Calculate liquidity ratios", description = "Calculates liquidity ratios for a company")
    public ResponseEntity<Map<String, Double>> calculateLiquidityRatios(@PathVariable Long companyId) {
        Map<String, Double> ratios = workingCapitalService.calculateLiquidityRatios(companyId);
        return ResponseEntity.ok(ratios);
    }

    /**
     * Endpoint to get historical working capital metrics for a date range.
     * 
     * @param companyId the company ID
     * @param startDate the start date
     * @param endDate the end date
     * @param interval the interval (DAILY, WEEKLY, MONTHLY)
     * @return a map of dates to metrics
     */
    @GetMapping("/historical/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Get historical metrics", description = "Retrieves historical working capital metrics for a date range")
    public ResponseEntity<Map<LocalDate, WorkingCapitalMetricsDto>> getHistoricalMetrics(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String interval) {
        
        // Validate the date range
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
        
        // Validate the interval
        if (!interval.equals("DAILY") && !interval.equals("WEEKLY") && !interval.equals("MONTHLY")) {
            throw new BadRequestException("Invalid interval. Valid values are DAILY, WEEKLY, MONTHLY");
        }
        
        Map<LocalDate, WorkingCapitalMetricsDto> historicalMetrics = 
                workingCapitalService.getHistoricalMetrics(companyId, startDate, endDate, interval);
        
        return ResponseEntity.ok(historicalMetrics);
    }

    /**
     * Endpoint to generate alerts for cash gaps or liquidity issues.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    @PostMapping("/generate-alerts/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Generate alerts", description = "Generates alerts for cash gaps or liquidity issues")
    public ResponseEntity<Map<String, Integer>> generateAlerts(@PathVariable Long companyId) {
        int alertsGenerated = workingCapitalService.generateAlerts(companyId);
        return ResponseEntity.ok(Map.of("alertsGenerated", alertsGenerated));
    }
}
