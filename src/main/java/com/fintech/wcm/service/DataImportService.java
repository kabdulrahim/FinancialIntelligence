package com.fintech.wcm.service;

import com.fintech.wcm.dto.ImportResultDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for data import operations.
 */
public interface DataImportService {
    
    /**
     * Import cash transactions from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    ImportResultDto importCashTransactions(Long companyId, MultipartFile file);
    
    /**
     * Import invoices from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    ImportResultDto importInvoices(Long companyId, MultipartFile file);
    
    /**
     * Import accounts receivable from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    ImportResultDto importAccountsReceivable(Long companyId, MultipartFile file);
    
    /**
     * Import accounts payable from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    ImportResultDto importAccountsPayable(Long companyId, MultipartFile file);
    
    /**
     * Import inventory from a CSV file.
     * 
     * @param companyId the company ID
     * @param file the CSV file
     * @return the import result
     */
    ImportResultDto importInventory(Long companyId, MultipartFile file);
    
    /**
     * Connect to QuickBooks and import data.
     * 
     * @param companyId the company ID
     * @param accessToken the QuickBooks access token
     * @param refreshToken the QuickBooks refresh token
     * @param realmId the QuickBooks realm ID
     * @return the import result
     */
    ImportResultDto importFromQuickBooks(Long companyId, String accessToken, String refreshToken, String realmId);
    
    /**
     * Connect to Xero and import data.
     * 
     * @param companyId the company ID
     * @param accessToken the Xero access token
     * @param tenantId the Xero tenant ID
     * @return the import result
     */
    ImportResultDto importFromXero(Long companyId, String accessToken, String tenantId);
    
    /**
     * Schedule a periodic data import job.
     * 
     * @param companyId the company ID
     * @param sourceType the source type (CSV, QUICKBOOKS, XERO, etc.)
     * @param cronExpression the cron expression for scheduling
     * @return the job ID
     */
    String scheduleImportJob(Long companyId, String sourceType, String cronExpression);
    
    /**
     * Cancel a scheduled import job.
     * 
     * @param jobId the job ID
     */
    void cancelScheduledImportJob(String jobId);
}
