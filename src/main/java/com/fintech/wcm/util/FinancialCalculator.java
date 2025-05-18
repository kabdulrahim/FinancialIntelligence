package com.fintech.wcm.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for financial calculations.
 */
@Component
public class FinancialCalculator {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculate net working capital.
     * 
     * @param currentAssets the current assets
     * @param currentLiabilities the current liabilities
     * @return the net working capital
     */
    public BigDecimal calculateNetWorkingCapital(BigDecimal currentAssets, BigDecimal currentLiabilities) {
        return currentAssets.subtract(currentLiabilities);
    }

    /**
     * Calculate the current ratio.
     * 
     * @param currentAssets the current assets
     * @param currentLiabilities the current liabilities
     * @return the current ratio, or null if current liabilities is zero
     */
    public BigDecimal calculateCurrentRatio(BigDecimal currentAssets, BigDecimal currentLiabilities) {
        if (currentLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        return currentAssets.divide(currentLiabilities, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate the quick ratio.
     * 
     * @param currentAssets the current assets
     * @param inventory the inventory
     * @param currentLiabilities the current liabilities
     * @return the quick ratio, or null if current liabilities is zero
     */
    public BigDecimal calculateQuickRatio(BigDecimal currentAssets, BigDecimal inventory, BigDecimal currentLiabilities) {
        if (currentLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal quickAssets = currentAssets.subtract(inventory);
        return quickAssets.divide(currentLiabilities, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate the cash ratio.
     * 
     * @param cashAndEquivalents the cash and equivalents
     * @param currentLiabilities the current liabilities
     * @return the cash ratio, or null if current liabilities is zero
     */
    public BigDecimal calculateCashRatio(BigDecimal cashAndEquivalents, BigDecimal currentLiabilities) {
        if (currentLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        return cashAndEquivalents.divide(currentLiabilities, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate Days Sales Outstanding (DSO).
     * 
     * @param accountsReceivable the accounts receivable
     * @param creditSales the credit sales
     * @param numberOfDays the number of days in the period
     * @return the DSO, or null if credit sales is zero
     */
    public BigDecimal calculateDSO(BigDecimal accountsReceivable, BigDecimal creditSales, int numberOfDays) {
        if (creditSales.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal dailySales = creditSales.divide(new BigDecimal(numberOfDays), 6, DEFAULT_ROUNDING_MODE);
        return accountsReceivable.divide(dailySales, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate Days Payable Outstanding (DPO).
     * 
     * @param accountsPayable the accounts payable
     * @param costOfGoodsSold the cost of goods sold
     * @param numberOfDays the number of days in the period
     * @return the DPO, or null if cost of goods sold is zero
     */
    public BigDecimal calculateDPO(BigDecimal accountsPayable, BigDecimal costOfGoodsSold, int numberOfDays) {
        if (costOfGoodsSold.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal dailyCOGS = costOfGoodsSold.divide(new BigDecimal(numberOfDays), 6, DEFAULT_ROUNDING_MODE);
        return accountsPayable.divide(dailyCOGS, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate Days Inventory Outstanding (DIO).
     * 
     * @param inventory the inventory
     * @param costOfGoodsSold the cost of goods sold
     * @param numberOfDays the number of days in the period
     * @return the DIO, or null if cost of goods sold is zero
     */
    public BigDecimal calculateDIO(BigDecimal inventory, BigDecimal costOfGoodsSold, int numberOfDays) {
        if (costOfGoodsSold.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal dailyCOGS = costOfGoodsSold.divide(new BigDecimal(numberOfDays), 6, DEFAULT_ROUNDING_MODE);
        return inventory.divide(dailyCOGS, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate Cash Conversion Cycle (CCC).
     * 
     * @param dso the Days Sales Outstanding
     * @param dio the Days Inventory Outstanding
     * @param dpo the Days Payable Outstanding
     * @return the CCC
     */
    public BigDecimal calculateCCC(BigDecimal dso, BigDecimal dio, BigDecimal dpo) {
        return dso.add(dio).subtract(dpo);
    }

    /**
     * Calculate the Working Capital Turnover Ratio.
     * 
     * @param revenue the revenue
     * @param averageWorkingCapital the average working capital
     * @return the working capital turnover ratio, or null if average working capital is zero
     */
    public BigDecimal calculateWorkingCapitalTurnover(BigDecimal revenue, BigDecimal averageWorkingCapital) {
        if (averageWorkingCapital.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        return revenue.divide(averageWorkingCapital, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate the number of days between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return the number of days between the dates
     */
    public long calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate the percentage change between two values.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     * @return the percentage change, or null if old value is zero
     */
    public BigDecimal calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal change = newValue.subtract(oldValue);
        return change.multiply(new BigDecimal("100")).divide(oldValue, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate the future value with compound interest.
     * 
     * @param presentValue the present value
     * @param interestRate the interest rate (as a decimal, e.g., 0.05 for 5%)
     * @param periods the number of periods
     * @return the future value
     */
    public BigDecimal calculateFutureValue(BigDecimal presentValue, BigDecimal interestRate, int periods) {
        BigDecimal rate = BigDecimal.ONE.add(interestRate);
        BigDecimal factor = rate.pow(periods);
        return presentValue.multiply(factor).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Calculate the break-even point.
     * 
     * @param fixedCosts the fixed costs
     * @param contributionMargin the contribution margin per unit
     * @return the break-even point in units, or null if contribution margin is zero
     */
    public BigDecimal calculateBreakEvenPoint(BigDecimal fixedCosts, BigDecimal contributionMargin) {
        if (contributionMargin.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        return fixedCosts.divide(contributionMargin, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
}
