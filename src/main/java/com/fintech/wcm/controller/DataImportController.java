package com.fintech.wcm.controller;

import com.fintech.wcm.dto.ImportResultDto;
import com.fintech.wcm.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for data import operations.
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Data Import", description = "Data Import API")
public class DataImportController {

    private final DataImportService dataImportService;

    /**
     * Endpoint to import cash transactions from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    @PostMapping("/cash-transactions/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import cash transactions", description = "Imports cash transactions from a CSV file")
    public ResponseEntity<ImportResultDto> importCashTransactions(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        ImportResultDto result = dataImportService.importCashTransactions(companyId, file);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to import invoices from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    @PostMapping("/invoices/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import invoices", description = "Imports invoices from a CSV file")
    public ResponseEntity<ImportResultDto> importInvoices(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        ImportResultDto result = dataImportService.importInvoices(companyId, file);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to import accounts receivable from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    @PostMapping("/accounts-receivable/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import accounts receivable", description = "Imports accounts receivable from a CSV file")
    public ResponseEntity<ImportResultDto> importAccountsReceivable(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        ImportResultDto result = dataImportService.importAccountsReceivable(companyId, file);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to import accounts payable from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    @PostMapping("/accounts-payable/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import accounts payable", description = "Imports accounts payable from a CSV file")
    public ResponseEntity<ImportResultDto> importAccountsPayable(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        ImportResultDto result = dataImportService.importAccountsPayable(companyId, file);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to import inventory from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    @PostMapping("/inventory/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or hasRole('ACCOUNTANT') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import inventory", description = "Imports inventory from a CSV file")
    public ResponseEntity<ImportResultDto> importInventory(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        ImportResultDto result = dataImportService.importInventory(companyId, file);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to import data from QuickBooks.
     * 
     * @param companyId the company ID
     * @param request the QuickBooks connection details
     * @return the import result
     */
    @PostMapping("/quickbooks/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import from QuickBooks", description = "Imports data from QuickBooks")
    public ResponseEntity<ImportResultDto> importFromQuickBooks(
            @PathVariable Long companyId,
            @RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        String refreshToken = request.get("refreshToken");
        String realmId = request.get("realmId");
        
        ImportResultDto result = dataImportService.importFromQuickBooks(companyId, accessToken, refreshToken, realmId);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to import data from Xero.
     * 
     * @param companyId the company ID
     * @param request the Xero connection details
     * @return the import result
     */
    @PostMapping("/xero/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Import from Xero", description = "Imports data from Xero")
    public ResponseEntity<ImportResultDto> importFromXero(
            @PathVariable Long companyId,
            @RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        String tenantId = request.get("tenantId");
        
        ImportResultDto result = dataImportService.importFromXero(companyId, accessToken, tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint to schedule a periodic data import job.
     * 
     * @param companyId the company ID
     * @param request the scheduling details
     * @return the job ID
     */
    @PostMapping("/schedule/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO') or @securityService.hasCompanyAccess(authentication, #companyId)")
    @Operation(summary = "Schedule import job", description = "Schedules a periodic data import job")
    public ResponseEntity<Map<String, String>> scheduleImportJob(
            @PathVariable Long companyId,
            @RequestBody Map<String, String> request) {
        String sourceType = request.get("sourceType");
        String cronExpression = request.get("cronExpression");
        
        String jobId = dataImportService.scheduleImportJob(companyId, sourceType, cronExpression);
        
        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to cancel a scheduled import job.
     * 
     * @param jobId the job ID
     * @return a response with no content
     */
    @DeleteMapping("/schedule/{jobId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CFO')")
    @Operation(summary = "Cancel scheduled import job", description = "Cancels a scheduled import job")
    public ResponseEntity<Void> cancelScheduledImportJob(@PathVariable String jobId) {
        dataImportService.cancelScheduledImportJob(jobId);
        return ResponseEntity.noContent().build();
    }
}
