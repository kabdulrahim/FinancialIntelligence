package com.fintech.wcm.service.impl;

import com.fintech.wcm.dto.ImportResultDto;
import com.fintech.wcm.exception.BadRequestException;
import com.fintech.wcm.exception.ResourceNotFoundException;
import com.fintech.wcm.model.*;
import com.fintech.wcm.repository.*;
import com.fintech.wcm.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Implementation of the DataImportService interface.
 */
@Service
@RequiredArgsConstructor
public class DataImportServiceImpl implements DataImportService {

    private static final Logger logger = LoggerFactory.getLogger(DataImportServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final CompanyRepository companyRepository;
    private final AccountsReceivableRepository accountsReceivableRepository;
    private final AccountsPayableRepository accountsPayableRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final CashAccountRepository cashAccountRepository;
    private final TaskScheduler taskScheduler;
    
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new HashMap<>();

    @Override
    @Transactional
    public ImportResultDto importCashTransactions(Long companyId, MultipartFile file) {
        Company company = validateCompanyAndFile(companyId, file);
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("CASH_TRANSACTIONS")
                .source("CSV")
                .fileName(file.getOriginalFilename())
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("COMPLETED")
                .build();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                result.setTotalRecords(result.getTotalRecords() + 1);
                
                try {
                    // Parse the CSV record
                    LocalDate transactionDate = parseDate(record.get("transaction_date"));
                    BigDecimal amount = new BigDecimal(record.get("amount"));
                    String description = record.get("description");
                    String transactionTypeStr = record.get("transaction_type");
                    String currencyCode = record.get("currency_code");
                    
                    Transaction.TransactionType transactionType;
                    try {
                        transactionType = Transaction.TransactionType.valueOf(transactionTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid transaction type: " + transactionTypeStr);
                    }
                    
                    // Create the transaction entity
                    Transaction transaction = new Transaction();
                    transaction.setCompany(company);
                    transaction.setTransactionDate(transactionDate);
                    transaction.setAmount(amount);
                    transaction.setDescription(description);
                    transaction.setTransactionType(transactionType);
                    transaction.setCurrencyCode(currencyCode);
                    
                    // Optional fields
                    if (record.isMapped("reference_number")) {
                        transaction.setReferenceNumber(record.get("reference_number"));
                    }
                    
                    if (record.isMapped("category")) {
                        transaction.setCategory(record.get("category"));
                    }
                    
                    if (record.isMapped("notes")) {
                        transaction.setNotes(record.get("notes"));
                    }
                    
                    if (record.isMapped("exchange_rate")) {
                        transaction.setExchangeRate(new BigDecimal(record.get("exchange_rate")));
                    }
                    
                    if (record.isMapped("amount_base_currency")) {
                        transaction.setAmountBaseCurrency(new BigDecimal(record.get("amount_base_currency")));
                    } else if (transaction.getExchangeRate() != null) {
                        // Calculate the base currency amount if exchange rate is provided
                        transaction.setAmountBaseCurrency(amount.multiply(transaction.getExchangeRate()));
                    } else {
                        // If no exchange rate, assume base currency
                        transaction.setAmountBaseCurrency(amount);
                    }
                    
                    if (record.isMapped("cash_account_id")) {
                        String cashAccountIdStr = record.get("cash_account_id");
                        if (cashAccountIdStr != null && !cashAccountIdStr.trim().isEmpty()) {
                            Long cashAccountId = Long.parseLong(cashAccountIdStr);
                            CashAccount cashAccount = cashAccountRepository.findById(cashAccountId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Cash account not found with id: " + cashAccountId));
                            transaction.setCashAccount(cashAccount);
                        }
                    }
                    
                    // Save the transaction
                    transactionRepository.save(transaction);
                    result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    
                } catch (Exception e) {
                    result.setFailedRecords(result.getFailedRecords() + 1);
                    result.addError("Error processing record " + result.getTotalRecords() + ": " + e.getMessage());
                    logger.error("Error processing transaction record: {}", e.getMessage());
                }
            }
            
            if (result.getFailedRecords() > 0) {
                result.setStatus("PARTIALLY_COMPLETED");
            }
            
            result.setSummary("Imported " + result.getSuccessfulRecords() + " out of " + result.getTotalRecords() + " cash transactions.");
            
        } catch (IOException e) {
            result.setStatus("FAILED");
            result.addError("Failed to read CSV file: " + e.getMessage());
            logger.error("Failed to read CSV file: {}", e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public ImportResultDto importInvoices(Long companyId, MultipartFile file) {
        Company company = validateCompanyAndFile(companyId, file);
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("INVOICES")
                .source("CSV")
                .fileName(file.getOriginalFilename())
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("COMPLETED")
                .build();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                result.setTotalRecords(result.getTotalRecords() + 1);
                
                try {
                    // Parse the CSV record
                    String invoiceNumber = record.get("invoice_number");
                    String invoiceTypeStr = record.get("invoice_type");
                    String contactName = record.get("contact_name");
                    LocalDate issueDate = parseDate(record.get("issue_date"));
                    LocalDate dueDate = parseDate(record.get("due_date"));
                    BigDecimal subtotal = new BigDecimal(record.get("subtotal"));
                    BigDecimal taxAmount = record.isMapped("tax_amount") && !record.get("tax_amount").isEmpty() ? 
                            new BigDecimal(record.get("tax_amount")) : BigDecimal.ZERO;
                    BigDecimal totalAmount = new BigDecimal(record.get("total_amount"));
                    String currencyCode = record.get("currency_code");
                    
                    Invoice.InvoiceType invoiceType;
                    try {
                        invoiceType = Invoice.InvoiceType.valueOf(invoiceTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid invoice type: " + invoiceTypeStr);
                    }
                    
                    // Create the invoice entity
                    Invoice invoice = new Invoice();
                    invoice.setCompany(company);
                    invoice.setInvoiceNumber(invoiceNumber);
                    invoice.setInvoiceType(invoiceType);
                    invoice.setContactName(contactName);
                    invoice.setIssueDate(issueDate);
                    invoice.setDueDate(dueDate);
                    invoice.setSubtotal(subtotal);
                    invoice.setTaxAmount(taxAmount);
                    invoice.setTotalAmount(totalAmount);
                    invoice.setCurrencyCode(currencyCode);
                    
                    // Optional fields
                    if (record.isMapped("contact_email")) {
                        invoice.setContactEmail(record.get("contact_email"));
                    }
                    
                    if (record.isMapped("payment_terms")) {
                        invoice.setPaymentTerms(record.get("payment_terms"));
                    }
                    
                    if (record.isMapped("notes")) {
                        invoice.setNotes(record.get("notes"));
                    }
                    
                    String statusStr = record.isMapped("status") ? record.get("status") : "SENT";
                    Invoice.InvoiceStatus status;
                    try {
                        status = Invoice.InvoiceStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        status = Invoice.InvoiceStatus.SENT;
                    }
                    invoice.setStatus(status);
                    
                    if (record.isMapped("exchange_rate")) {
                        invoice.setExchangeRate(new BigDecimal(record.get("exchange_rate")));
                    }
                    
                    if (record.isMapped("total_amount_base_currency")) {
                        invoice.setTotalAmountBaseCurrency(new BigDecimal(record.get("total_amount_base_currency")));
                    } else if (invoice.getExchangeRate() != null) {
                        // Calculate the base currency amount if exchange rate is provided
                        invoice.setTotalAmountBaseCurrency(totalAmount.multiply(invoice.getExchangeRate()));
                    } else {
                        // If no exchange rate, assume base currency
                        invoice.setTotalAmountBaseCurrency(totalAmount);
                    }
                    
                    // Save the invoice
                    invoiceRepository.save(invoice);
                    result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    
                } catch (Exception e) {
                    result.setFailedRecords(result.getFailedRecords() + 1);
                    result.addError("Error processing record " + result.getTotalRecords() + ": " + e.getMessage());
                    logger.error("Error processing invoice record: {}", e.getMessage());
                }
            }
            
            if (result.getFailedRecords() > 0) {
                result.setStatus("PARTIALLY_COMPLETED");
            }
            
            result.setSummary("Imported " + result.getSuccessfulRecords() + " out of " + result.getTotalRecords() + " invoices.");
            
        } catch (IOException e) {
            result.setStatus("FAILED");
            result.addError("Failed to read CSV file: " + e.getMessage());
            logger.error("Failed to read CSV file: {}", e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public ImportResultDto importAccountsReceivable(Long companyId, MultipartFile file) {
        Company company = validateCompanyAndFile(companyId, file);
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("ACCOUNTS_RECEIVABLE")
                .source("CSV")
                .fileName(file.getOriginalFilename())
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("COMPLETED")
                .build();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                result.setTotalRecords(result.getTotalRecords() + 1);
                
                try {
                    // Parse the CSV record
                    String customerName = record.get("customer_name");
                    BigDecimal amount = new BigDecimal(record.get("amount"));
                    String currencyCode = record.get("currency_code");
                    LocalDate invoiceDate = parseDate(record.get("invoice_date"));
                    LocalDate dueDate = parseDate(record.get("due_date"));
                    String invoiceNumber = record.get("invoice_number");
                    String statusStr = record.get("status");
                    
                    AccountsReceivable.ReceivableStatus status;
                    try {
                        status = AccountsReceivable.ReceivableStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid receivable status: " + statusStr);
                    }
                    
                    // Create the accounts receivable entity
                    AccountsReceivable receivable = new AccountsReceivable();
                    receivable.setCompany(company);
                    receivable.setCustomerName(customerName);
                    receivable.setAmount(amount);
                    receivable.setCurrencyCode(currencyCode);
                    receivable.setInvoiceDate(invoiceDate);
                    receivable.setDueDate(dueDate);
                    receivable.setInvoiceNumber(invoiceNumber);
                    receivable.setStatus(status);
                    
                    // Optional fields
                    if (record.isMapped("notes")) {
                        receivable.setNotes(record.get("notes"));
                    }
                    
                    if (record.isMapped("payment_terms")) {
                        receivable.setPaymentTerms(record.get("payment_terms"));
                    }
                    
                    if (record.isMapped("exchange_rate")) {
                        receivable.setExchangeRate(new BigDecimal(record.get("exchange_rate")));
                    }
                    
                    if (record.isMapped("amount_base_currency")) {
                        receivable.setAmountBaseCurrency(new BigDecimal(record.get("amount_base_currency")));
                    } else if (receivable.getExchangeRate() != null) {
                        // Calculate the base currency amount if exchange rate is provided
                        receivable.setAmountBaseCurrency(amount.multiply(receivable.getExchangeRate()));
                    } else {
                        // If no exchange rate, assume base currency
                        receivable.setAmountBaseCurrency(amount);
                    }
                    
                    // Save the accounts receivable
                    accountsReceivableRepository.save(receivable);
                    result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    
                } catch (Exception e) {
                    result.setFailedRecords(result.getFailedRecords() + 1);
                    result.addError("Error processing record " + result.getTotalRecords() + ": " + e.getMessage());
                    logger.error("Error processing accounts receivable record: {}", e.getMessage());
                }
            }
            
            if (result.getFailedRecords() > 0) {
                result.setStatus("PARTIALLY_COMPLETED");
            }
            
            result.setSummary("Imported " + result.getSuccessfulRecords() + " out of " + result.getTotalRecords() + " accounts receivable entries.");
            
        } catch (IOException e) {
            result.setStatus("FAILED");
            result.addError("Failed to read CSV file: " + e.getMessage());
            logger.error("Failed to read CSV file: {}", e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public ImportResultDto importAccountsPayable(Long companyId, MultipartFile file) {
        Company company = validateCompanyAndFile(companyId, file);
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("ACCOUNTS_PAYABLE")
                .source("CSV")
                .fileName(file.getOriginalFilename())
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("COMPLETED")
                .build();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                result.setTotalRecords(result.getTotalRecords() + 1);
                
                try {
                    // Parse the CSV record
                    String vendorName = record.get("vendor_name");
                    BigDecimal amount = new BigDecimal(record.get("amount"));
                    String currencyCode = record.get("currency_code");
                    LocalDate invoiceDate = parseDate(record.get("invoice_date"));
                    LocalDate dueDate = parseDate(record.get("due_date"));
                    String invoiceNumber = record.get("invoice_number");
                    String statusStr = record.get("status");
                    
                    AccountsPayable.PayableStatus status;
                    try {
                        status = AccountsPayable.PayableStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid payable status: " + statusStr);
                    }
                    
                    // Create the accounts payable entity
                    AccountsPayable payable = new AccountsPayable();
                    payable.setCompany(company);
                    payable.setVendorName(vendorName);
                    payable.setAmount(amount);
                    payable.setCurrencyCode(currencyCode);
                    payable.setInvoiceDate(invoiceDate);
                    payable.setDueDate(dueDate);
                    payable.setInvoiceNumber(invoiceNumber);
                    payable.setStatus(status);
                    
                    // Optional fields
                    if (record.isMapped("category")) {
                        payable.setCategory(record.get("category"));
                    }
                    
                    if (record.isMapped("notes")) {
                        payable.setNotes(record.get("notes"));
                    }
                    
                    if (record.isMapped("payment_terms")) {
                        payable.setPaymentTerms(record.get("payment_terms"));
                    }
                    
                    if (record.isMapped("exchange_rate")) {
                        payable.setExchangeRate(new BigDecimal(record.get("exchange_rate")));
                    }
                    
                    if (record.isMapped("amount_base_currency")) {
                        payable.setAmountBaseCurrency(new BigDecimal(record.get("amount_base_currency")));
                    } else if (payable.getExchangeRate() != null) {
                        // Calculate the base currency amount if exchange rate is provided
                        payable.setAmountBaseCurrency(amount.multiply(payable.getExchangeRate()));
                    } else {
                        // If no exchange rate, assume base currency
                        payable.setAmountBaseCurrency(amount);
                    }
                    
                    // Save the accounts payable
                    accountsPayableRepository.save(payable);
                    result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    
                } catch (Exception e) {
                    result.setFailedRecords(result.getFailedRecords() + 1);
                    result.addError("Error processing record " + result.getTotalRecords() + ": " + e.getMessage());
                    logger.error("Error processing accounts payable record: {}", e.getMessage());
                }
            }
            
            if (result.getFailedRecords() > 0) {
                result.setStatus("PARTIALLY_COMPLETED");
            }
            
            result.setSummary("Imported " + result.getSuccessfulRecords() + " out of " + result.getTotalRecords() + " accounts payable entries.");
            
        } catch (IOException e) {
            result.setStatus("FAILED");
            result.addError("Failed to read CSV file: " + e.getMessage());
            logger.error("Failed to read CSV file: {}", e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public ImportResultDto importInventory(Long companyId, MultipartFile file) {
        Company company = validateCompanyAndFile(companyId, file);
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("INVENTORY")
                .source("CSV")
                .fileName(file.getOriginalFilename())
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("COMPLETED")
                .build();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                result.setTotalRecords(result.getTotalRecords() + 1);
                
                try {
                    // Parse the CSV record
                    String itemName = record.get("item_name");
                    Integer quantity = Integer.parseInt(record.get("quantity"));
                    BigDecimal unitCost = new BigDecimal(record.get("unit_cost"));
                    BigDecimal totalValue = new BigDecimal(record.get("total_value"));
                    String currencyCode = record.get("currency_code");
                    String itemTypeStr = record.get("item_type");
                    
                    Inventory.ItemType itemType;
                    try {
                        itemType = Inventory.ItemType.valueOf(itemTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Invalid item type: " + itemTypeStr);
                    }
                    
                    // Create the inventory entity
                    Inventory inventory = new Inventory();
                    inventory.setCompany(company);
                    inventory.setItemName(itemName);
                    inventory.setQuantity(quantity);
                    inventory.setUnitCost(unitCost);
                    inventory.setTotalValue(totalValue);
                    inventory.setCurrencyCode(currencyCode);
                    inventory.setItemType(itemType);
                    
                    // Optional fields
                    if (record.isMapped("item_code")) {
                        inventory.setItemCode(record.get("item_code"));
                    }
                    
                    if (record.isMapped("acquisition_date")) {
                        inventory.setAcquisitionDate(parseDate(record.get("acquisition_date")));
                    }
                    
                    if (record.isMapped("location")) {
                        inventory.setLocation(record.get("location"));
                    }
                    
                    if (record.isMapped("description")) {
                        inventory.setDescription(record.get("description"));
                    }
                    
                    if (record.isMapped("reorder_level")) {
                        inventory.setReorderLevel(Integer.parseInt(record.get("reorder_level")));
                    }
                    
                    if (record.isMapped("status")) {
                        String statusStr = record.get("status");
                        try {
                            Inventory.InventoryStatus status = Inventory.InventoryStatus.valueOf(statusStr.toUpperCase());
                            inventory.setStatus(status);
                        } catch (IllegalArgumentException e) {
                            inventory.setStatus(Inventory.InventoryStatus.IN_STOCK);
                        }
                    } else {
                        inventory.setStatus(Inventory.InventoryStatus.IN_STOCK);
                    }
                    
                    // Save the inventory item
                    inventoryRepository.save(inventory);
                    result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    
                } catch (Exception e) {
                    result.setFailedRecords(result.getFailedRecords() + 1);
                    result.addError("Error processing record " + result.getTotalRecords() + ": " + e.getMessage());
                    logger.error("Error processing inventory record: {}", e.getMessage());
                }
            }
            
            if (result.getFailedRecords() > 0) {
                result.setStatus("PARTIALLY_COMPLETED");
            }
            
            result.setSummary("Imported " + result.getSuccessfulRecords() + " out of " + result.getTotalRecords() + " inventory items.");
            
        } catch (IOException e) {
            result.setStatus("FAILED");
            result.addError("Failed to read CSV file: " + e.getMessage());
            logger.error("Failed to read CSV file: {}", e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public ImportResultDto importFromQuickBooks(Long companyId, String accessToken, String refreshToken, String realmId) {
        // In a real implementation, this would connect to the QuickBooks API
        // For now, we'll return a placeholder result
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("QUICKBOOKS_DATA")
                .source("QUICKBOOKS_API")
                .fileName(null)
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("FAILED")
                .build();
        
        result.addError("QuickBooks API integration is not implemented yet.");
        result.setSummary("QuickBooks API integration is not implemented yet.");
        
        return result;
    }

    @Override
    @Transactional
    public ImportResultDto importFromXero(Long companyId, String accessToken, String tenantId) {
        // In a real implementation, this would connect to the Xero API
        // For now, we'll return a placeholder result
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        ImportResultDto result = ImportResultDto.builder()
                .importType("XERO_DATA")
                .source("XERO_API")
                .fileName(null)
                .importDate(LocalDateTime.now())
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .status("FAILED")
                .build();
        
        result.addError("Xero API integration is not implemented yet.");
        result.setSummary("Xero API integration is not implemented yet.");
        
        return result;
    }

    @Override
    public String scheduleImportJob(Long companyId, String sourceType, String cronExpression) {
        // Validate company
        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        // Generate a job ID
        String jobId = "import_" + companyId + "_" + sourceType + "_" + System.currentTimeMillis();
        
        // Schedule the job
        Runnable task = () -> {
            logger.info("Executing scheduled import job: {}", jobId);
            // In a real implementation, this would perform the actual import based on the source type
        };
        
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, new CronTrigger(cronExpression));
        scheduledJobs.put(jobId, scheduledTask);
        
        return jobId;
    }

    @Override
    public void cancelScheduledImportJob(String jobId) {
        ScheduledFuture<?> scheduledTask = scheduledJobs.get(jobId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledJobs.remove(jobId);
            logger.info("Cancelled scheduled import job: {}", jobId);
        } else {
            throw new ResourceNotFoundException("Scheduled import job not found with id: " + jobId);
        }
    }
    
    /**
     * Validates the company and file.
     * 
     * @param companyId the company ID
     * @param file the file to import
     * @return the Company entity
     */
    private Company validateCompanyAndFile(Long companyId, MultipartFile file) {
        // Check if company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        // Check if file is empty
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        
        // Check if file is a CSV
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("File is not a CSV file");
        }
        
        return company;
    }
    
    /**
     * Parses a date string to a LocalDate.
     * 
     * @param dateStr the date string
     * @return the LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format: " + dateStr + ". Expected format: yyyy-MM-dd");
        }
    }
}
