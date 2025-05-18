package com.fintech.wcm.service.impl;

import com.fintech.wcm.exception.ResourceNotFoundException;
import com.fintech.wcm.model.Alert;
import com.fintech.wcm.model.Company;
import com.fintech.wcm.repository.*;
import com.fintech.wcm.service.AlertService;
import com.fintech.wcm.service.WorkingCapitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the AlertService interface.
 */
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final CompanyRepository companyRepository;
    private final AlertRepository alertRepository;
    private final CashAccountRepository cashAccountRepository;
    private final AccountsReceivableRepository accountsReceivableRepository;
    private final AccountsPayableRepository accountsPayableRepository;
    private final ShortTermLiabilityRepository shortTermLiabilityRepository;
    private final WorkingCapitalService workingCapitalService;

    @Override
    @Transactional
    public Alert createAlert(Alert alert) {
        // Set created time if not already set
        if (alert.getCreatedAt() == null) {
            alert.setCreatedAt(LocalDateTime.now());
        }
        return alertRepository.save(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAlertsByCompanyId(Long companyId) {
        return alertRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getActiveAlertsByCompanyId(Long companyId) {
        return alertRepository.findByCompanyIdAndDismissedFalse(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getUnreadAlertsByCompanyId(Long companyId) {
        return alertRepository.findByCompanyIdAndReadFalse(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAlertsByCompanyIdAndType(Long companyId, Alert.AlertType alertType) {
        return alertRepository.findByCompanyIdAndAlertType(companyId, alertType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAlertsByCompanyIdAndSeverity(Long companyId, Alert.AlertSeverity severity) {
        return alertRepository.findByCompanyIdAndSeverity(companyId, severity);
    }

    @Override
    @Transactional
    public void markAlertAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        
        alert.setRead(true);
        alert.setReadAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public void dismissAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        
        alert.setDismissed(true);
        alert.setDismissedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public int generateCashGapAlerts(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        int alertsGenerated = 0;
        
        // Get current cash balance
        BigDecimal cashBalance = cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId);
        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }
        
        // Get upcoming payables for the next 30 days
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        
        List<AccountsPayable.PayableStatus> unpaidStatuses = List.of(
                AccountsPayable.PayableStatus.PENDING, 
                AccountsPayable.PayableStatus.APPROVED,
                AccountsPayable.PayableStatus.PARTIALLY_PAID,
                AccountsPayable.PayableStatus.OVERDUE
        );
        
        BigDecimal upcomingPayables = BigDecimal.ZERO;
        List<AccountsPayable> payables = accountsPayableRepository.findByCompanyIdAndDueDateBetweenAndStatusIn(
                companyId, today, thirtyDaysLater, unpaidStatuses);
        
        for (AccountsPayable payable : payables) {
            upcomingPayables = upcomingPayables.add(payable.getAmountBaseCurrency());
        }
        
        // Get expected receivables for the next 30 days
        List<AccountsReceivable.ReceivableStatus> unpaidReceivableStatuses = List.of(
                AccountsReceivable.ReceivableStatus.OPEN,
                AccountsReceivable.ReceivableStatus.PARTIALLY_PAID
        );
        
        BigDecimal expectedReceivables = BigDecimal.ZERO;
        List<AccountsReceivable> receivables = accountsReceivableRepository.findByCompanyIdAndDueDateBeforeAndStatusNot(
                companyId, thirtyDaysLater, AccountsReceivable.ReceivableStatus.PAID);
        
        for (AccountsReceivable receivable : receivables) {
            if (unpaidReceivableStatuses.contains(receivable.getStatus())) {
                expectedReceivables = expectedReceivables.add(receivable.getAmountBaseCurrency());
            }
        }
        
        // Calculate projected cash balance
        BigDecimal projectedBalance = cashBalance.add(expectedReceivables).subtract(upcomingPayables);
        
        // Create alert if projected balance is negative
        if (projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Potential Cash Gap Detected");
            alert.setMessage("Your projected cash balance in 30 days is " + projectedBalance + " " + 
                    company.getCurrencyCode() + ". Current balance: " + cashBalance + ", Upcoming payables: " + 
                    upcomingPayables + ", Expected receivables: " + expectedReceivables);
            alert.setAlertType(Alert.AlertType.CASH_GAP);
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Projected Cash Balance");
            alert.setTriggerThreshold("0");
            alert.setTriggerValue(projectedBalance.toString());
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check for severely low cash balance
        if (cashBalance.compareTo(new BigDecimal("10000")) < 0) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Low Cash Balance");
            alert.setMessage("Your current cash balance is " + cashBalance + " " + company.getCurrencyCode() + 
                    ", which is below the recommended minimum of 10,000 " + company.getCurrencyCode());
            alert.setAlertType(Alert.AlertType.CASH_GAP);
            alert.setSeverity(Alert.AlertSeverity.MEDIUM);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Cash Balance");
            alert.setTriggerThreshold("10000");
            alert.setTriggerValue(cashBalance.toString());
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        return alertsGenerated;
    }

    @Override
    @Transactional
    public int generateLiquidityAlerts(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        int alertsGenerated = 0;
        
        // Calculate liquidity ratios
        Map<String, Double> liquidityRatios = workingCapitalService.calculateLiquidityRatios(companyId);
        
        // Check current ratio
        double currentRatio = liquidityRatios.getOrDefault("currentRatio", 0.0);
        if (currentRatio < 1.0) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Critical Current Ratio");
            alert.setMessage("Your current ratio is " + currentRatio + ", which is below 1.0. This indicates " +
                    "potential inability to meet short-term obligations.");
            alert.setAlertType(Alert.AlertType.LIQUIDITY_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.CRITICAL);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Current Ratio");
            alert.setTriggerThreshold("1.0");
            alert.setTriggerValue(String.valueOf(currentRatio));
            
            createAlert(alert);
            alertsGenerated++;
        } else if (currentRatio < 1.5) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Low Current Ratio");
            alert.setMessage("Your current ratio is " + currentRatio + ", which is below the recommended minimum of 1.5.");
            alert.setAlertType(Alert.AlertType.LIQUIDITY_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.MEDIUM);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Current Ratio");
            alert.setTriggerThreshold("1.5");
            alert.setTriggerValue(String.valueOf(currentRatio));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check quick ratio
        double quickRatio = liquidityRatios.getOrDefault("quickRatio", 0.0);
        if (quickRatio < 1.0) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Low Quick Ratio");
            alert.setMessage("Your quick ratio is " + quickRatio + ", which is below 1.0. This indicates " +
                    "potential liquidity issues without relying on inventory.");
            alert.setAlertType(Alert.AlertType.LIQUIDITY_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Quick Ratio");
            alert.setTriggerThreshold("1.0");
            alert.setTriggerValue(String.valueOf(quickRatio));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check cash ratio
        double cashRatio = liquidityRatios.getOrDefault("cashRatio", 0.0);
        if (cashRatio < 0.2) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Low Cash Ratio");
            alert.setMessage("Your cash ratio is " + cashRatio + ", which is below 0.2. This indicates " +
                    "potential issues meeting immediate obligations with cash.");
            alert.setAlertType(Alert.AlertType.LIQUIDITY_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.MEDIUM);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Cash Ratio");
            alert.setTriggerThreshold("0.2");
            alert.setTriggerValue(String.valueOf(cashRatio));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        return alertsGenerated;
    }

    @Override
    @Transactional
    public int generateWorkingCapitalRatioAlerts(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        int alertsGenerated = 0;
        
        // Calculate net working capital
        BigDecimal cashAndEquivalents = cashAccountRepository.sumTotalCashBalanceByCompanyId(companyId);
        if (cashAndEquivalents == null) cashAndEquivalents = BigDecimal.ZERO;
        
        BigDecimal accountsReceivable = accountsReceivableRepository.sumTotalReceivablesByCompanyId(companyId);
        if (accountsReceivable == null) accountsReceivable = BigDecimal.ZERO;
        
        BigDecimal inventory = BigDecimal.ZERO; // Assuming we don't have inventory data
        
        BigDecimal totalCurrentAssets = cashAndEquivalents.add(accountsReceivable).add(inventory);
        
        BigDecimal accountsPayable = accountsPayableRepository.sumTotalPayablesByCompanyId(companyId);
        if (accountsPayable == null) accountsPayable = BigDecimal.ZERO;
        
        BigDecimal shortTermDebt = shortTermLiabilityRepository.sumTotalLiabilitiesByCompanyId(companyId);
        if (shortTermDebt == null) shortTermDebt = BigDecimal.ZERO;
        
        BigDecimal totalCurrentLiabilities = accountsPayable.add(shortTermDebt);
        
        BigDecimal netWorkingCapital = totalCurrentAssets.subtract(totalCurrentLiabilities);
        
        // Create alert if net working capital is negative
        if (netWorkingCapital.compareTo(BigDecimal.ZERO) < 0) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Negative Working Capital");
            alert.setMessage("Your net working capital is " + netWorkingCapital + " " + company.getCurrencyCode() + 
                    ". Negative working capital indicates potential financial distress.");
            alert.setAlertType(Alert.AlertType.WORKING_CAPITAL_RATIO);
            alert.setSeverity(Alert.AlertSeverity.CRITICAL);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Net Working Capital");
            alert.setTriggerThreshold("0");
            alert.setTriggerValue(netWorkingCapital.toString());
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check if current ratio is too low
        if (totalCurrentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentRatio = totalCurrentAssets.divide(totalCurrentLiabilities, 2, BigDecimal.ROUND_HALF_UP);
            
            if (currentRatio.compareTo(new BigDecimal("1.0")) < 0) {
                Alert alert = new Alert();
                alert.setCompany(company);
                alert.setTitle("Critical Current Ratio");
                alert.setMessage("Your current ratio is " + currentRatio + ", which is below 1.0. This indicates " +
                        "potential inability to meet short-term obligations.");
                alert.setAlertType(Alert.AlertType.WORKING_CAPITAL_RATIO);
                alert.setSeverity(Alert.AlertSeverity.CRITICAL);
                alert.setRead(false);
                alert.setDismissed(false);
                alert.setTriggerMetric("Current Ratio");
                alert.setTriggerThreshold("1.0");
                alert.setTriggerValue(currentRatio.toString());
                
                createAlert(alert);
                alertsGenerated++;
            }
        }
        
        return alertsGenerated;
    }

    @Override
    @Transactional
    public int generateCCCAlerts(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        int alertsGenerated = 0;
        
        // Calculate CCC
        double dso = workingCapitalService.calculateDSO(companyId);
        double dpo = workingCapitalService.calculateDPO(companyId);
        double dio = workingCapitalService.calculateDIO(companyId);
        double ccc = workingCapitalService.calculateCCC(companyId);
        
        // Create alert if CCC is too high (industry benchmarks would be better, but using a generic threshold here)
        if (ccc > 90) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("High Cash Conversion Cycle");
            alert.setMessage("Your Cash Conversion Cycle is " + String.format("%.2f", ccc) + " days, which is above " +
                    "the recommended maximum of 90 days. DSO: " + String.format("%.2f", dso) + " days, DPO: " + 
                    String.format("%.2f", dpo) + " days, DIO: " + String.format("%.2f", dio) + " days.");
            alert.setAlertType(Alert.AlertType.CCC_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Cash Conversion Cycle");
            alert.setTriggerThreshold("90");
            alert.setTriggerValue(String.valueOf(ccc));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check for high DSO
        if (dso > 45) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("High Days Sales Outstanding");
            alert.setMessage("Your Days Sales Outstanding is " + String.format("%.2f", dso) + " days, which is above " +
                    "the recommended maximum of 45 days. Consider reviewing your credit policies and collection processes.");
            alert.setAlertType(Alert.AlertType.CCC_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.MEDIUM);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Days Sales Outstanding");
            alert.setTriggerThreshold("45");
            alert.setTriggerValue(String.valueOf(dso));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check for low DPO
        if (dpo < 30 && dpo > 0) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("Low Days Payable Outstanding");
            alert.setMessage("Your Days Payable Outstanding is " + String.format("%.2f", dpo) + " days, which is below " +
                    "the recommended minimum of 30 days. Consider negotiating longer payment terms with suppliers.");
            alert.setAlertType(Alert.AlertType.CCC_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.LOW);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Days Payable Outstanding");
            alert.setTriggerThreshold("30");
            alert.setTriggerValue(String.valueOf(dpo));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        // Check for high DIO
        if (dio > 60) {
            Alert alert = new Alert();
            alert.setCompany(company);
            alert.setTitle("High Days Inventory Outstanding");
            alert.setMessage("Your Days Inventory Outstanding is " + String.format("%.2f", dio) + " days, which is above " +
                    "the recommended maximum of 60 days. Consider implementing just-in-time inventory management.");
            alert.setAlertType(Alert.AlertType.CCC_ISSUE);
            alert.setSeverity(Alert.AlertSeverity.MEDIUM);
            alert.setRead(false);
            alert.setDismissed(false);
            alert.setTriggerMetric("Days Inventory Outstanding");
            alert.setTriggerThreshold("60");
            alert.setTriggerValue(String.valueOf(dio));
            
            createAlert(alert);
            alertsGenerated++;
        }
        
        return alertsGenerated;
    }
}
