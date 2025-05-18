package com.fintech.wcm.service.impl;

import com.fintech.wcm.dto.DashboardSummaryDto;
import com.fintech.wcm.dto.WorkingCapitalMetricsDto;
import com.fintech.wcm.exception.ResourceNotFoundException;
import com.fintech.wcm.model.*;
import com.fintech.wcm.repository.*;
import com.fintech.wcm.service.AlertService;
import com.fintech.wcm.service.WorkingCapitalService;
import com.fintech.wcm.util.FinancialCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the WorkingCapitalService interface.
 */
@Service
@RequiredArgsConstructor
public class WorkingCapitalServiceImpl implements WorkingCapitalService {

    private final CompanyRepository companyRepository;
    private final CashAccountRepository cashAccountRepository;
    private final AccountsReceivableRepository accountsReceivableRepository;
    private final AccountsPayableRepository accountsPayableRepository;
    private final InventoryRepository inventoryRepository;
    private final ShortTermLiabilityRepository shortTermLiabilityRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final AlertService alertService;
    private final FinancialCalculator financialCalculator;

    @Override
    @Transactional(readOnly = true)
    public WorkingCapitalMetricsDto calculateWorkingCapitalMetrics(Long companyId) {
        return calculateWorkingCapitalMetrics(companyId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkingCapitalMetricsDto calculateWorkingCapitalMetrics(Long companyId, LocalDate asOfDate) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        // Create the metrics DTO with company information
        WorkingCapitalMetricsDto metricsDto = WorkingCapitalMetricsDto.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .calculationDate(asOfDate)
                .build();
        
        // Calculate current assets
        BigDecimal cashAndEquivalents = cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId);
        if (cashAndEquivalents == null) cashAndEquivalents = BigDecimal.ZERO;
        
        BigDecimal accountsReceivable = accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId);
        if (accountsReceivable == null) accountsReceivable = BigDecimal.ZERO;
        
        BigDecimal inventory = inventoryRepository.sumTotalInventoryValueByCompanyId(companyId);
        if (inventory == null) inventory = BigDecimal.ZERO;
        
        // Calculate current liabilities
        BigDecimal accountsPayable = accountsPayableRepository.sumTotalPayablesByCompanyId(companyId);
        if (accountsPayable == null) accountsPayable = BigDecimal.ZERO;
        
        BigDecimal shortTermDebt = shortTermLiabilityRepository.sumTotalLiabilitiesByCompanyId(companyId);
        if (shortTermDebt == null) shortTermDebt = BigDecimal.ZERO;
        
        // Calculate the total current assets and liabilities
        BigDecimal totalCurrentAssets = cashAndEquivalents.add(accountsReceivable).add(inventory);
        BigDecimal totalCurrentLiabilities = accountsPayable.add(shortTermDebt);
        
        // Calculate net working capital
        BigDecimal netWorkingCapital = totalCurrentAssets.subtract(totalCurrentLiabilities);
        
        // Set the calculated values in the metrics DTO
        metricsDto.setTotalCurrentAssets(totalCurrentAssets);
        metricsDto.setCashAndEquivalents(cashAndEquivalents);
        metricsDto.setAccountsReceivable(accountsReceivable);
        metricsDto.setInventory(inventory);
        metricsDto.setOtherCurrentAssets(BigDecimal.ZERO); // Placeholder for other assets
        
        metricsDto.setTotalCurrentLiabilities(totalCurrentLiabilities);
        metricsDto.setAccountsPayable(accountsPayable);
        metricsDto.setShortTermDebt(shortTermDebt);
        metricsDto.setOtherCurrentLiabilities(BigDecimal.ZERO); // Placeholder for other liabilities
        
        metricsDto.setNetWorkingCapital(netWorkingCapital);
        
        // Calculate financial ratios
        if (totalCurrentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentRatio = totalCurrentAssets.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP);
            metricsDto.setCurrentRatio(currentRatio);
            
            BigDecimal quickAssets = totalCurrentAssets.subtract(inventory);
            BigDecimal quickRatio = quickAssets.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP);
            metricsDto.setQuickRatio(quickRatio);
        }
        
        // Calculate CCC components
        double dso = calculateDSO(companyId);
        double dpo = calculateDPO(companyId);
        double dio = calculateDIO(companyId);
        double ccc = calculateCCC(companyId);
        
        metricsDto.setDaysSalesOutstanding(BigDecimal.valueOf(dso).setScale(2, RoundingMode.HALF_UP));
        metricsDto.setDaysPayablesOutstanding(BigDecimal.valueOf(dpo).setScale(2, RoundingMode.HALF_UP));
        metricsDto.setDaysInventoryOutstanding(BigDecimal.valueOf(dio).setScale(2, RoundingMode.HALF_UP));
        metricsDto.setCashConversionCycle(BigDecimal.valueOf(ccc).setScale(2, RoundingMode.HALF_UP));
        
        // Calculate trend indicators
        // This would typically involve comparing with previous periods
        // For now, we'll use placeholders
        metricsDto.setWorkingCapitalTrend("STABLE");
        metricsDto.setCashConversionCycleTrend("STABLE");
        metricsDto.setLiquidityTrend("STABLE");
        
        return metricsDto;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        LocalDate asOfDate = LocalDate.now();
        
        // Create the dashboard summary DTO with company information
        DashboardSummaryDto summaryDto = DashboardSummaryDto.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .companyType(company.getType().name())
                .currencyCode(company.getCurrencyCode())
                .asOfDate(asOfDate)
                .build();
        
        // Populate key metrics
        BigDecimal cashBalance = cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId);
        if (cashBalance == null) cashBalance = BigDecimal.ZERO;
        
        BigDecimal accountsReceivable = accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId);
        if (accountsReceivable == null) accountsReceivable = BigDecimal.ZERO;
        
        BigDecimal accountsPayable = accountsPayableRepository.sumTotalPayablesByCompanyId(companyId);
        if (accountsPayable == null) accountsPayable = BigDecimal.ZERO;
        
        BigDecimal inventory = inventoryRepository.sumTotalInventoryValueByCompanyId(companyId);
        if (inventory == null) inventory = BigDecimal.ZERO;
        
        BigDecimal totalCurrentAssets = cashBalance.add(accountsReceivable).add(inventory);
        BigDecimal totalCurrentLiabilities = accountsPayable;
        
        BigDecimal netWorkingCapital = totalCurrentAssets.subtract(totalCurrentLiabilities);
        
        summaryDto.setCashBalance(cashBalance);
        summaryDto.setAccountsReceivable(accountsReceivable);
        summaryDto.setAccountsPayable(accountsPayable);
        summaryDto.setInventory(inventory);
        summaryDto.setNetWorkingCapital(netWorkingCapital);
        
        // Financial ratios
        if (totalCurrentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentRatio = totalCurrentAssets.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP);
            summaryDto.setCurrentRatio(currentRatio);
            
            BigDecimal quickAssets = totalCurrentAssets.subtract(inventory);
            BigDecimal quickRatio = quickAssets.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP);
            summaryDto.setQuickRatio(quickRatio);
        }
        
        // Cash conversion cycle components
        double dso = calculateDSO(companyId);
        double dpo = calculateDPO(companyId);
        double dio = calculateDIO(companyId);
        double ccc = calculateCCC(companyId);
        
        summaryDto.setDaysSalesOutstanding(BigDecimal.valueOf(dso).setScale(2, RoundingMode.HALF_UP));
        summaryDto.setDaysPayableOutstanding(BigDecimal.valueOf(dpo).setScale(2, RoundingMode.HALF_UP));
        summaryDto.setDaysInventoryOutstanding(BigDecimal.valueOf(dio).setScale(2, RoundingMode.HALF_UP));
        summaryDto.setCashConversionCycle(BigDecimal.valueOf(ccc).setScale(2, RoundingMode.HALF_UP));
        
        // Alert information
        long totalAlerts = alertRepository.countByCompanyIdAndReadFalse(companyId);
        long criticalAlerts = alertRepository.findByCompanyIdAndSeverity(companyId, Alert.AlertSeverity.CRITICAL).size();
        long highAlerts = alertRepository.findByCompanyIdAndSeverity(companyId, Alert.AlertSeverity.HIGH).size();
        
        summaryDto.setTotalAlerts((int) totalAlerts);
        summaryDto.setCriticalAlerts((int) criticalAlerts);
        summaryDto.setHighAlerts((int) highAlerts);
        
        // Recent alerts
        List<Alert> recentAlerts = alertRepository.findByCompanyIdAndDismissedFalse(companyId).stream()
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> recentAlertsList = new ArrayList<>();
        for (Alert alert : recentAlerts) {
            Map<String, Object> alertMap = new HashMap<>();
            alertMap.put("id", alert.getId());
            alertMap.put("title", alert.getTitle());
            alertMap.put("message", alert.getMessage());
            alertMap.put("alertType", alert.getAlertType().name());
            alertMap.put("severity", alert.getSeverity().name());
            alertMap.put("createdAt", alert.getCreatedAt());
            recentAlertsList.add(alertMap);
        }
        summaryDto.setRecentAlerts(recentAlertsList);
        
        // Upcoming cash flow
        LocalDate thirtyDaysLater = asOfDate.plusDays(30);
        
        List<AccountsPayable.PayableStatus> unpaidStatuses = Arrays.asList(
                AccountsPayable.PayableStatus.PENDING, 
                AccountsPayable.PayableStatus.APPROVED,
                AccountsPayable.PayableStatus.PARTIALLY_PAID
        );
        
        List<AccountsPayable> upcomingPayables = accountsPayableRepository.findByCompanyIdAndDueDateBetweenAndStatusIn(
                companyId, asOfDate, thirtyDaysLater, unpaidStatuses);
        
        BigDecimal upcomingPayables30Days = upcomingPayables.stream()
                .map(AccountsPayable::getAmountBaseCurrency)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<AccountsReceivable.ReceivableStatus> unpaidReceivableStatuses = Arrays.asList(
                AccountsReceivable.ReceivableStatus.OPEN,
                AccountsReceivable.ReceivableStatus.PARTIALLY_PAID
        );
        
        List<AccountsReceivable> upcomingReceivables = accountsReceivableRepository.findByCompanyIdAndDueDateBeforeAndStatusNot(
                companyId, thirtyDaysLater, AccountsReceivable.ReceivableStatus.PAID);
        
        BigDecimal upcomingReceivables30Days = upcomingReceivables.stream()
                .filter(ar -> unpaidReceivableStatuses.contains(ar.getStatus()))
                .map(AccountsReceivable::getAmountBaseCurrency)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal projectedCashBalance30Days = cashBalance.add(upcomingReceivables30Days).subtract(upcomingPayables30Days);
        
        summaryDto.setUpcomingReceivables30Days(upcomingReceivables30Days);
        summaryDto.setUpcomingPayables30Days(upcomingPayables30Days);
        summaryDto.setProjectedCashBalance30Days(projectedCashBalance30Days);
        
        // Top customers and vendors
        List<Object[]> topCustomers = accountsReceivableRepository.findReceivablesByCustomer(companyId);
        List<Object[]> topVendors = accountsPayableRepository.findPayablesByVendor(companyId);
        
        Map<String, Object> topCustomersMap = new HashMap<>();
        List<Map<String, Object>> customersList = new ArrayList<>();
        
        for (int i = 0; i < Math.min(5, topCustomers.size()); i++) {
            Object[] customer = topCustomers.get(i);
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("name", customer[0]);
            customerMap.put("amount", customer[1]);
            customersList.add(customerMap);
        }
        
        topCustomersMap.put("customers", customersList);
        summaryDto.setTopCustomers(topCustomersMap);
        
        Map<String, Object> topVendorsMap = new HashMap<>();
        List<Map<String, Object>> vendorsList = new ArrayList<>();
        
        for (int i = 0; i < Math.min(5, topVendors.size()); i++) {
            Object[] vendor = topVendors.get(i);
            Map<String, Object> vendorMap = new HashMap<>();
            vendorMap.put("name", vendor[0]);
            vendorMap.put("amount", vendor[1]);
            vendorsList.add(vendorMap);
        }
        
        topVendorsMap.put("vendors", vendorsList);
        summaryDto.setTopVendors(topVendorsMap);
        
        // Working capital trend (mock data for now)
        Map<String, Object> wcTrend = new HashMap<>();
        wcTrend.put("current", netWorkingCapital);
        wcTrend.put("previous", netWorkingCapital.multiply(new BigDecimal("0.95")));
        wcTrend.put("trend", "IMPROVING");
        summaryDto.setWorkingCapitalTrend(wcTrend);
        
        // Recommendations based on metrics
        List<Map<String, Object>> recommendations = generateRecommendations(companyId, summaryDto);
        summaryDto.setRecommendations(recommendations);
        
        return summaryDto;
    }

    @Override
    public double calculateDSO(Long companyId) {
        // Get accounts receivable balance
        BigDecimal arBalance = accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId);
        if (arBalance == null || arBalance.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        // Get average daily sales for the last 90 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);
        
        BigDecimal totalSales = invoiceRepository.sumTotalOpenSalesInvoicesByCompanyId(companyId);
        if (totalSales == null || totalSales.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        double daysInPeriod = 90.0;
        double averageDailySales = totalSales.doubleValue() / daysInPeriod;
        
        if (averageDailySales == 0.0) {
            return 0.0;
        }
        
        return arBalance.doubleValue() / averageDailySales;
    }

    @Override
    public double calculateDPO(Long companyId) {
        // Get accounts payable balance
        BigDecimal apBalance = accountsPayableRepository.sumTotalPayablesByCompanyId(companyId);
        if (apBalance == null || apBalance.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        // Get average daily purchases for the last 90 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);
        
        BigDecimal totalPurchases = invoiceRepository.sumTotalOpenPurchaseInvoicesByCompanyId(companyId);
        if (totalPurchases == null || totalPurchases.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        double daysInPeriod = 90.0;
        double averageDailyPurchases = totalPurchases.doubleValue() / daysInPeriod;
        
        if (averageDailyPurchases == 0.0) {
            return 0.0;
        }
        
        return apBalance.doubleValue() / averageDailyPurchases;
    }

    @Override
    public double calculateDIO(Long companyId) {
        // Get inventory value
        BigDecimal inventoryValue = inventoryRepository.sumTotalInventoryValueByCompanyId(companyId);
        if (inventoryValue == null || inventoryValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        // Get cost of goods sold for the last 90 days
        // For this simplified implementation, we'll use purchase invoices as a proxy for COGS
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90);
        
        BigDecimal costOfGoodsSold = invoiceRepository.sumTotalOpenPurchaseInvoicesByCompanyId(companyId);
        if (costOfGoodsSold == null || costOfGoodsSold.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        double daysInPeriod = 90.0;
        double averageDailyCOGS = costOfGoodsSold.doubleValue() / daysInPeriod;
        
        if (averageDailyCOGS == 0.0) {
            return 0.0;
        }
        
        return inventoryValue.doubleValue() / averageDailyCOGS;
    }

    @Override
    public double calculateCCC(Long companyId) {
        double dso = calculateDSO(companyId);
        double dpo = calculateDPO(companyId);
        double dio = calculateDIO(companyId);
        
        return dio + dso - dpo;
    }

    @Override
    public Map<String, Double> calculateLiquidityRatios(Long companyId) {
        Map<String, Double> liquidityRatios = new HashMap<>();
        
        // Get current assets and liabilities
        BigDecimal cashAndEquivalents = cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId);
        if (cashAndEquivalents == null) cashAndEquivalents = BigDecimal.ZERO;
        
        BigDecimal accountsReceivable = accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId);
        if (accountsReceivable == null) accountsReceivable = BigDecimal.ZERO;
        
        BigDecimal inventory = inventoryRepository.sumTotalInventoryValueByCompanyId(companyId);
        if (inventory == null) inventory = BigDecimal.ZERO;
        
        BigDecimal totalCurrentAssets = cashAndEquivalents.add(accountsReceivable).add(inventory);
        
        BigDecimal accountsPayable = accountsPayableRepository.sumTotalPayablesByCompanyId(companyId);
        if (accountsPayable == null) accountsPayable = BigDecimal.ZERO;
        
        BigDecimal shortTermDebt = shortTermLiabilityRepository.sumTotalLiabilitiesByCompanyId(companyId);
        if (shortTermDebt == null) shortTermDebt = BigDecimal.ZERO;
        
        BigDecimal totalCurrentLiabilities = accountsPayable.add(shortTermDebt);
        
        // Calculate current ratio
        if (totalCurrentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            double currentRatio = totalCurrentAssets.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP).doubleValue();
            liquidityRatios.put("currentRatio", currentRatio);
        } else {
            liquidityRatios.put("currentRatio", 0.0);
        }
        
        // Calculate quick ratio
        if (totalCurrentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal quickAssets = totalCurrentAssets.subtract(inventory);
            double quickRatio = quickAssets.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP).doubleValue();
            liquidityRatios.put("quickRatio", quickRatio);
        } else {
            liquidityRatios.put("quickRatio", 0.0);
        }
        
        // Calculate cash ratio
        if (totalCurrentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            double cashRatio = cashAndEquivalents.divide(totalCurrentLiabilities, 2, RoundingMode.HALF_UP).doubleValue();
            liquidityRatios.put("cashRatio", cashRatio);
        } else {
            liquidityRatios.put("cashRatio", 0.0);
        }
        
        return liquidityRatios;
    }

    @Override
    public Map<LocalDate, WorkingCapitalMetricsDto> getHistoricalMetrics(Long companyId, LocalDate startDate, LocalDate endDate, String interval) {
        Map<LocalDate, WorkingCapitalMetricsDto> historicalMetrics = new HashMap<>();
        
        // Determine how many data points to create based on the interval
        List<LocalDate> datesToCalculate = new ArrayList<>();
        
        switch (interval.toUpperCase()) {
            case "DAILY":
                long daysCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                for (int i = 0; i < daysCount; i++) {
                    datesToCalculate.add(startDate.plusDays(i));
                }
                break;
            case "WEEKLY":
                // Calculate for the start of each week
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    datesToCalculate.add(currentDate);
                    currentDate = currentDate.plusWeeks(1);
                }
                break;
            case "MONTHLY":
                // Calculate for the start of each month
                LocalDate monthDate = startDate.withDayOfMonth(1);
                while (!monthDate.isAfter(endDate)) {
                    if (!monthDate.isBefore(startDate)) {
                        datesToCalculate.add(monthDate);
                    }
                    monthDate = monthDate.plusMonths(1);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid interval: " + interval + ". Valid values are DAILY, WEEKLY, MONTHLY");
        }
        
        // Calculate metrics for each date
        for (LocalDate date : datesToCalculate) {
            WorkingCapitalMetricsDto metrics = calculateWorkingCapitalMetrics(companyId, date);
            historicalMetrics.put(date, metrics);
        }
        
        return historicalMetrics;
    }

    @Override
    @Transactional
    public int generateAlerts(Long companyId) {
        int alertsGenerated = 0;
        
        alertsGenerated += alertService.generateCashGapAlerts(companyId);
        alertsGenerated += alertService.generateLiquidityAlerts(companyId);
        alertsGenerated += alertService.generateWorkingCapitalRatioAlerts(companyId);
        alertsGenerated += alertService.generateCCCAlerts(companyId);
        
        return alertsGenerated;
    }
    
    /**
     * Generates recommendations based on the dashboard metrics.
     * 
     * @param companyId the company ID
     * @param summary the dashboard summary
     * @return a list of recommendation objects
     */
    private List<Map<String, Object>> generateRecommendations(Long companyId, DashboardSummaryDto summary) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // Check cash balance
        if (summary.getCashBalance().compareTo(BigDecimal.ZERO) == 0 || 
                summary.getCashBalance().compareTo(new BigDecimal("10000")) < 0) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "CASH_BALANCE");
            recommendation.put("title", "Improve Cash Balance");
            recommendation.put("description", "Your cash balance is low. Consider invoicing customers earlier or negotiating longer payment terms with suppliers.");
            recommendation.put("priority", "HIGH");
            recommendations.add(recommendation);
        }
        
        // Check accounts receivable
        if (summary.getDaysSalesOutstanding().compareTo(new BigDecimal("45")) > 0) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "DSO");
            recommendation.put("title", "Reduce Days Sales Outstanding");
            recommendation.put("description", "Your DSO is high at " + summary.getDaysSalesOutstanding() + " days. Implement stricter credit policies and improve collections process.");
            recommendation.put("priority", "MEDIUM");
            recommendations.add(recommendation);
        }
        
        // Check accounts payable
        if (summary.getDaysPayableOutstanding().compareTo(new BigDecimal("20")) < 0) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "DPO");
            recommendation.put("title", "Optimize Days Payable Outstanding");
            recommendation.put("description", "Your DPO is low at " + summary.getDaysPayableOutstanding() + " days. Consider negotiating longer payment terms with suppliers.");
            recommendation.put("priority", "MEDIUM");
            recommendations.add(recommendation);
        }
        
        // Check inventory
        if (summary.getDaysInventoryOutstanding().compareTo(new BigDecimal("60")) > 0) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "DIO");
            recommendation.put("title", "Reduce Inventory Days");
            recommendation.put("description", "Your inventory turnover is slow at " + summary.getDaysInventoryOutstanding() + " days. Implement just-in-time inventory management.");
            recommendation.put("priority", "MEDIUM");
            recommendations.add(recommendation);
        }
        
        // Check cash conversion cycle
        if (summary.getCashConversionCycle().compareTo(new BigDecimal("75")) > 0) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "CCC");
            recommendation.put("title", "Improve Cash Conversion Cycle");
            recommendation.put("description", "Your CCC is high at " + summary.getCashConversionCycle() + " days. Focus on reducing inventory and receivables while extending payables.");
            recommendation.put("priority", "HIGH");
            recommendations.add(recommendation);
        }
        
        // Check current ratio
        if (summary.getCurrentRatio() != null && summary.getCurrentRatio().compareTo(new BigDecimal("1.2")) < 0) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "CURRENT_RATIO");
            recommendation.put("title", "Improve Current Ratio");
            recommendation.put("description", "Your current ratio is low at " + summary.getCurrentRatio() + ". This indicates potential liquidity issues.");
            recommendation.put("priority", "HIGH");
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }
}
