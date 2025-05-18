package com.fintech.wcm.controller;

import com.fintech.wcm.dto.DashboardSummaryDto;
import com.fintech.wcm.model.Alert;
import com.fintech.wcm.service.AlertService;
import com.fintech.wcm.service.WorkingCapitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for dashboard-related endpoints.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Dashboard", description = "Dashboard API")
public class DashboardController {

    private final WorkingCapitalService workingCapitalService;
    private final AlertService alertService;

    /**
     * Endpoint to get the dashboard summary for a company.
     * 
     * @param companyId the company ID
     * @return the dashboard summary
     */
    @GetMapping("/summary/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Get dashboard summary", description = "Retrieves the dashboard summary for a company")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(@PathVariable Long companyId) {
        DashboardSummaryDto summary = workingCapitalService.getDashboardSummary(companyId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Endpoint to get alerts for a company.
     * 
     * @param companyId the company ID
     * @return a list of alerts
     */
    @GetMapping("/alerts/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Get alerts", description = "Retrieves alerts for a company")
    public ResponseEntity<List<Alert>> getAlerts(@PathVariable Long companyId) {
        List<Alert> alerts = alertService.getAlertsByCompanyId(companyId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Endpoint to get unread alerts for a company.
     * 
     * @param companyId the company ID
     * @return a list of unread alerts
     */
    @GetMapping("/alerts/{companyId}/unread")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Get unread alerts", description = "Retrieves unread alerts for a company")
    public ResponseEntity<List<Alert>> getUnreadAlerts(@PathVariable Long companyId) {
        List<Alert> alerts = alertService.getUnreadAlertsByCompanyId(companyId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Endpoint to mark an alert as read.
     * 
     * @param alertId the alert ID
     * @return a response with no content
     */
    @PostMapping("/alerts/{alertId}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or hasRole('OWNER')")
    @Operation(summary = "Mark alert as read", description = "Marks an alert as read")
    public ResponseEntity<Void> markAlertAsRead(@PathVariable Long alertId) {
        alertService.markAlertAsRead(alertId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to dismiss an alert.
     * 
     * @param alertId the alert ID
     * @return a response with no content
     */
    @PostMapping("/alerts/{alertId}/dismiss")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or hasRole('OWNER')")
    @Operation(summary = "Dismiss alert", description = "Dismisses an alert")
    public ResponseEntity<Void> dismissAlert(@PathVariable Long alertId) {
        alertService.dismissAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to get alerts by type.
     * 
     * @param companyId the company ID
     * @param type the alert type
     * @return a list of alerts
     */
    @GetMapping("/alerts/{companyId}/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Get alerts by type", description = "Retrieves alerts of a specific type for a company")
    public ResponseEntity<List<Alert>> getAlertsByType(
            @PathVariable Long companyId,
            @PathVariable String type) {
        Alert.AlertType alertType = Alert.AlertType.valueOf(type);
        List<Alert> alerts = alertService.getAlertsByCompanyIdAndType(companyId, alertType);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Endpoint to get alerts by severity.
     * 
     * @param companyId the company ID
     * @param severity the alert severity
     * @return a list of alerts
     */
    @GetMapping("/alerts/{companyId}/severity/{severity}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Get alerts by severity", description = "Retrieves alerts of a specific severity for a company")
    public ResponseEntity<List<Alert>> getAlertsBySeverity(
            @PathVariable Long companyId,
            @PathVariable String severity) {
        Alert.AlertSeverity alertSeverity = Alert.AlertSeverity.valueOf(severity);
        List<Alert> alerts = alertService.getAlertsByCompanyIdAndSeverity(companyId, alertSeverity);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Endpoint to generate cash gap alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    @PostMapping("/alerts/{companyId}/generate/cash-gap")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Generate cash gap alerts", description = "Generates cash gap alerts for a company")
    public ResponseEntity<Map<String, Integer>> generateCashGapAlerts(@PathVariable Long companyId) {
        int alertsGenerated = alertService.generateCashGapAlerts(companyId);
        return ResponseEntity.ok(Map.of("alertsGenerated", alertsGenerated));
    }

    /**
     * Endpoint to generate liquidity alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    @PostMapping("/alerts/{companyId}/generate/liquidity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Generate liquidity alerts", description = "Generates liquidity alerts for a company")
    public ResponseEntity<Map<String, Integer>> generateLiquidityAlerts(@PathVariable Long companyId) {
        int alertsGenerated = alertService.generateLiquidityAlerts(companyId);
        return ResponseEntity.ok(Map.of("alertsGenerated", alertsGenerated));
    }

    /**
     * Endpoint to generate working capital ratio alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    @PostMapping("/alerts/{companyId}/generate/working-capital-ratio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Generate working capital ratio alerts", description = "Generates working capital ratio alerts for a company")
    public ResponseEntity<Map<String, Integer>> generateWorkingCapitalRatioAlerts(@PathVariable Long companyId) {
        int alertsGenerated = alertService.generateWorkingCapitalRatioAlerts(companyId);
        return ResponseEntity.ok(Map.of("alertsGenerated", alertsGenerated));
    }

    /**
     * Endpoint to generate Cash Conversion Cycle (CCC) alerts for a company.
     * 
     * @param companyId the company ID
     * @return the number of alerts generated
     */
    @PostMapping("/alerts/{companyId}/generate/ccc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Generate CCC alerts", description = "Generates Cash Conversion Cycle alerts for a company")
    public ResponseEntity<Map<String, Integer>> generateCCCAlerts(@PathVariable Long companyId) {
        int alertsGenerated = alertService.generateCCCAlerts(companyId);
        return ResponseEntity.ok(Map.of("alertsGenerated", alertsGenerated));
    }
}
