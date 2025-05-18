package com.fintech.wcm.service;

import com.fintech.wcm.dto.DashboardSummaryDto;
import com.fintech.wcm.dto.WorkingCapitalMetricsDto;
import com.fintech.wcm.model.Company;
import com.fintech.wcm.repository.*;
import com.fintech.wcm.service.impl.WorkingCapitalServiceImpl;
import com.fintech.wcm.util.FinancialCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the WorkingCapitalService implementation.
 */
@ExtendWith(MockitoExtension.class)
public class WorkingCapitalServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    
    @Mock
    private CashAccountRepository cashAccountRepository;
    
    @Mock
    private AccountsReceivableRepository accountsReceivableRepository;
    
    @Mock
    private AccountsPayableRepository accountsPayableRepository;
    
    @Mock
    private InventoryRepository inventoryRepository;
    
    @Mock
    private ShortTermLiabilityRepository shortTermLiabilityRepository;
    
    @Mock
    private InvoiceRepository invoiceRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private AlertRepository alertRepository;
    
    @Mock
    private AlertService alertService;
    
    @Mock
    private FinancialCalculator financialCalculator;
    
    @InjectMocks
    private WorkingCapitalServiceImpl workingCapitalService;
    
    private Company testCompany;
    private final Long companyId = 1L;
    
    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(companyId);
        testCompany.setName("Test Company");
        testCompany.setType(Company.CompanyType.SME);
        testCompany.setCurrencyCode("USD");
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(testCompany));
    }
    
    @Test
    void calculateWorkingCapitalMetrics_ShouldReturnCorrectMetrics() {
        // Mock repositories to return test data
        when(cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId))
                .thenReturn(new BigDecimal("50000.00"));
        
        when(accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId))
                .thenReturn(new BigDecimal("75000.00"));
        
        when(inventoryRepository.sumTotalInventoryValueByCompanyId(companyId))
                .thenReturn(new BigDecimal("100000.00"));
        
        when(accountsPayableRepository.sumTotalPayablesByCompanyId(companyId))
                .thenReturn(new BigDecimal("60000.00"));
        
        when(shortTermLiabilityRepository.sumTotalLiabilitiesByCompanyId(companyId))
                .thenReturn(new BigDecimal("40000.00"));
        
        // Calculate metrics
        WorkingCapitalMetricsDto metrics = workingCapitalService.calculateWorkingCapitalMetrics(companyId);
        
        // Verify the results
        assertNotNull(metrics);
        assertEquals(companyId, metrics.getCompanyId());
        assertEquals("Test Company", metrics.getCompanyName());
        assertEquals(LocalDate.now(), metrics.getCalculationDate());
        
        // Verify assets
        assertEquals(new BigDecimal("225000.00"), metrics.getTotalCurrentAssets());
        assertEquals(new BigDecimal("50000.00"), metrics.getCashAndEquivalents());
        assertEquals(new BigDecimal("75000.00"), metrics.getAccountsReceivable());
        assertEquals(new BigDecimal("100000.00"), metrics.getInventory());
        
        // Verify liabilities
        assertEquals(new BigDecimal("100000.00"), metrics.getTotalCurrentLiabilities());
        assertEquals(new BigDecimal("60000.00"), metrics.getAccountsPayable());
        assertEquals(new BigDecimal("40000.00"), metrics.getShortTermDebt());
        
        // Verify working capital
        assertEquals(new BigDecimal("125000.00"), metrics.getNetWorkingCapital());
        
        // The current ratio should be 2.25
        assertEquals(new BigDecimal("2.25"), metrics.getCurrentRatio());
        
        // The quick ratio should be 1.25 (225000 - 100000) / 100000
        assertEquals(new BigDecimal("1.25"), metrics.getQuickRatio());
    }
    
    @Test
    void getDashboardSummary_ShouldReturnCorrectSummary() {
        // Mock repositories to return test data
        when(cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId))
                .thenReturn(new BigDecimal("50000.00"));
        
        when(accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId))
                .thenReturn(new BigDecimal("75000.00"));
        
        when(inventoryRepository.sumTotalInventoryValueByCompanyId(companyId))
                .thenReturn(new BigDecimal("100000.00"));
        
        when(accountsPayableRepository.sumTotalPayablesByCompanyId(companyId))
                .thenReturn(new BigDecimal("60000.00"));
        
        when(alertRepository.countByCompanyIdAndReadFalse(companyId)).thenReturn(5L);
        
        // Get dashboard summary
        DashboardSummaryDto summary = workingCapitalService.getDashboardSummary(companyId);
        
        // Verify the results
        assertNotNull(summary);
        assertEquals(companyId, summary.getCompanyId());
        assertEquals("Test Company", summary.getCompanyName());
        assertEquals("SME", summary.getCompanyType());
        assertEquals("USD", summary.getCurrencyCode());
        assertEquals(LocalDate.now(), summary.getAsOfDate());
        
        // Verify key metrics
        assertEquals(new BigDecimal("50000.00"), summary.getCashBalance());
        assertEquals(new BigDecimal("75000.00"), summary.getAccountsReceivable());
        assertEquals(new BigDecimal("60000.00"), summary.getAccountsPayable());
        assertEquals(new BigDecimal("100000.00"), summary.getInventory());
        
        // Verify working capital
        BigDecimal netWorkingCapital = new BigDecimal("165000.00"); // 50000 + 75000 + 100000 - 60000
        assertEquals(netWorkingCapital, summary.getNetWorkingCapital());
        
        // Verify alerts count
        assertEquals(5, summary.getTotalAlerts());
    }
    
    @Test
    void calculateLiquidityRatios_ShouldReturnCorrectRatios() {
        // Mock repositories to return test data
        when(cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId))
                .thenReturn(new BigDecimal("50000.00"));
        
        when(accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId))
                .thenReturn(new BigDecimal("75000.00"));
        
        when(inventoryRepository.sumTotalInventoryValueByCompanyId(companyId))
                .thenReturn(new BigDecimal("100000.00"));
        
        when(accountsPayableRepository.sumTotalPayablesByCompanyId(companyId))
                .thenReturn(new BigDecimal("60000.00"));
        
        when(shortTermLiabilityRepository.sumTotalLiabilitiesByCompanyId(companyId))
                .thenReturn(new BigDecimal("40000.00"));
        
        // Calculate liquidity ratios
        Map<String, Double> ratios = workingCapitalService.calculateLiquidityRatios(companyId);
        
        // Verify the results
        assertNotNull(ratios);
        
        // The current ratio should be 2.25 (225000 / 100000)
        assertEquals(2.25, ratios.get("currentRatio"));
        
        // The quick ratio should be 1.25 (225000 - 100000) / 100000
        assertEquals(1.25, ratios.get("quickRatio"));
        
        // The cash ratio should be 0.50 (50000 / 100000)
        assertEquals(0.50, ratios.get("cashRatio"));
    }
    
    @Test
    void generateAlerts_ShouldCallAlertServices() {
        // Mock alert service methods to return counts
        when(alertService.generateCashGapAlerts(companyId)).thenReturn(2);
        when(alertService.generateLiquidityAlerts(companyId)).thenReturn(1);
        when(alertService.generateWorkingCapitalRatioAlerts(companyId)).thenReturn(3);
        when(alertService.generateCCCAlerts(companyId)).thenReturn(2);
        
        // Generate alerts
        int alertsGenerated = workingCapitalService.generateAlerts(companyId);
        
        // Verify the result
        assertEquals(8, alertsGenerated);
    }
}
