package com.fintech.wcm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FinancialCalculator utility class.
 */
public class FinancialCalculatorTest {

    private FinancialCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FinancialCalculator();
    }

    @Test
    void calculateNetWorkingCapital_ShouldReturnCorrectValue() {
        // Test with positive net working capital
        BigDecimal currentAssets = new BigDecimal("225000.00");
        BigDecimal currentLiabilities = new BigDecimal("100000.00");
        BigDecimal expected = new BigDecimal("125000.00");
        
        BigDecimal result = calculator.calculateNetWorkingCapital(currentAssets, currentLiabilities);
        
        assertEquals(expected, result);
        
        // Test with negative net working capital
        currentAssets = new BigDecimal("80000.00");
        currentLiabilities = new BigDecimal("100000.00");
        expected = new BigDecimal("-20000.00");
        
        result = calculator.calculateNetWorkingCapital(currentAssets, currentLiabilities);
        
        assertEquals(expected, result);
    }

    @Test
    void calculateCurrentRatio_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal currentAssets = new BigDecimal("225000.00");
        BigDecimal currentLiabilities = new BigDecimal("100000.00");
        BigDecimal expected = new BigDecimal("2.25");
        
        BigDecimal result = calculator.calculateCurrentRatio(currentAssets, currentLiabilities);
        
        assertEquals(expected, result);
        
        // Test with zero liabilities (should return null)
        currentLiabilities = BigDecimal.ZERO;
        
        result = calculator.calculateCurrentRatio(currentAssets, currentLiabilities);
        
        assertNull(result);
    }

    @Test
    void calculateQuickRatio_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal currentAssets = new BigDecimal("225000.00");
        BigDecimal inventory = new BigDecimal("100000.00");
        BigDecimal currentLiabilities = new BigDecimal("100000.00");
        BigDecimal expected = new BigDecimal("1.25");
        
        BigDecimal result = calculator.calculateQuickRatio(currentAssets, inventory, currentLiabilities);
        
        assertEquals(expected, result);
        
        // Test with zero liabilities (should return null)
        currentLiabilities = BigDecimal.ZERO;
        
        result = calculator.calculateQuickRatio(currentAssets, inventory, currentLiabilities);
        
        assertNull(result);
    }

    @Test
    void calculateCashRatio_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal cashAndEquivalents = new BigDecimal("50000.00");
        BigDecimal currentLiabilities = new BigDecimal("100000.00");
        BigDecimal expected = new BigDecimal("0.50");
        
        BigDecimal result = calculator.calculateCashRatio(cashAndEquivalents, currentLiabilities);
        
        assertEquals(expected, result);
        
        // Test with zero liabilities (should return null)
        currentLiabilities = BigDecimal.ZERO;
        
        result = calculator.calculateCashRatio(cashAndEquivalents, currentLiabilities);
        
        assertNull(result);
    }

    @Test
    void calculateDSO_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal accountsReceivable = new BigDecimal("75000.00");
        BigDecimal creditSales = new BigDecimal("450000.00");
        int numberOfDays = 90;
        BigDecimal expected = new BigDecimal("15.00");
        
        BigDecimal result = calculator.calculateDSO(accountsReceivable, creditSales, numberOfDays);
        
        assertEquals(expected, result);
        
        // Test with zero credit sales (should return null)
        creditSales = BigDecimal.ZERO;
        
        result = calculator.calculateDSO(accountsReceivable, creditSales, numberOfDays);
        
        assertNull(result);
    }

    @Test
    void calculateDPO_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal accountsPayable = new BigDecimal("60000.00");
        BigDecimal costOfGoodsSold = new BigDecimal("360000.00");
        int numberOfDays = 90;
        BigDecimal expected = new BigDecimal("15.00");
        
        BigDecimal result = calculator.calculateDPO(accountsPayable, costOfGoodsSold, numberOfDays);
        
        assertEquals(expected, result);
        
        // Test with zero cost of goods sold (should return null)
        costOfGoodsSold = BigDecimal.ZERO;
        
        result = calculator.calculateDPO(accountsPayable, costOfGoodsSold, numberOfDays);
        
        assertNull(result);
    }

    @Test
    void calculateDIO_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal inventory = new BigDecimal("100000.00");
        BigDecimal costOfGoodsSold = new BigDecimal("360000.00");
        int numberOfDays = 90;
        BigDecimal expected = new BigDecimal("25.00");
        
        BigDecimal result = calculator.calculateDIO(inventory, costOfGoodsSold, numberOfDays);
        
        assertEquals(expected, result);
        
        // Test with zero cost of goods sold (should return null)
        costOfGoodsSold = BigDecimal.ZERO;
        
        result = calculator.calculateDIO(inventory, costOfGoodsSold, numberOfDays);
        
        assertNull(result);
    }

    @Test
    void calculateCCC_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal dso = new BigDecimal("45.00");
        BigDecimal dio = new BigDecimal("60.00");
        BigDecimal dpo = new BigDecimal("30.00");
        BigDecimal expected = new BigDecimal("75.00");
        
        BigDecimal result = calculator.calculateCCC(dso, dio, dpo);
        
        assertEquals(expected, result);
    }

    @Test
    void calculateWorkingCapitalTurnover_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal revenue = new BigDecimal("1000000.00");
        BigDecimal averageWorkingCapital = new BigDecimal("200000.00");
        BigDecimal expected = new BigDecimal("5.00");
        
        BigDecimal result = calculator.calculateWorkingCapitalTurnover(revenue, averageWorkingCapital);
        
        assertEquals(expected, result);
        
        // Test with zero average working capital (should return null)
        averageWorkingCapital = BigDecimal.ZERO;
        
        result = calculator.calculateWorkingCapitalTurnover(revenue, averageWorkingCapital);
        
        assertNull(result);
    }

    @Test
    void calculateDaysBetween_ShouldReturnCorrectValue() {
        // Test with same date (should return 0)
        LocalDate date1 = LocalDate.of(2023, 1, 1);
        LocalDate date2 = LocalDate.of(2023, 1, 1);
        
        long result = calculator.calculateDaysBetween(date1, date2);
        
        assertEquals(0, result);
        
        // Test with different dates
        date1 = LocalDate.of(2023, 1, 1);
        date2 = LocalDate.of(2023, 1, 31);
        
        result = calculator.calculateDaysBetween(date1, date2);
        
        assertEquals(30, result);
        
        // Test with dates in different years
        date1 = LocalDate.of(2022, 12, 1);
        date2 = LocalDate.of(2023, 1, 1);
        
        result = calculator.calculateDaysBetween(date1, date2);
        
        assertEquals(31, result);
    }

    @Test
    void calculatePercentageChange_ShouldReturnCorrectValue() {
        // Test with increase
        BigDecimal oldValue = new BigDecimal("100.00");
        BigDecimal newValue = new BigDecimal("120.00");
        BigDecimal expected = new BigDecimal("20.00");
        
        BigDecimal result = calculator.calculatePercentageChange(oldValue, newValue);
        
        assertEquals(expected, result);
        
        // Test with decrease
        oldValue = new BigDecimal("100.00");
        newValue = new BigDecimal("80.00");
        expected = new BigDecimal("-20.00");
        
        result = calculator.calculatePercentageChange(oldValue, newValue);
        
        assertEquals(expected, result);
        
        // Test with no change
        oldValue = new BigDecimal("100.00");
        newValue = new BigDecimal("100.00");
        expected = new BigDecimal("0.00");
        
        result = calculator.calculatePercentageChange(oldValue, newValue);
        
        assertEquals(expected, result);
        
        // Test with zero old value (should return null)
        oldValue = BigDecimal.ZERO;
        newValue = new BigDecimal("100.00");
        
        result = calculator.calculatePercentageChange(oldValue, newValue);
        
        assertNull(result);
    }

    @Test
    void calculateFutureValue_ShouldReturnCorrectValue() {
        // Test with positive interest rate
        BigDecimal presentValue = new BigDecimal("1000.00");
        BigDecimal interestRate = new BigDecimal("0.05");
        int periods = 3;
        BigDecimal expected = new BigDecimal("1157.63");
        
        BigDecimal result = calculator.calculateFutureValue(presentValue, interestRate, periods);
        
        assertEquals(expected, result);
        
        // Test with zero interest rate
        interestRate = BigDecimal.ZERO;
        expected = new BigDecimal("1000.00");
        
        result = calculator.calculateFutureValue(presentValue, interestRate, periods);
        
        assertEquals(expected, result);
    }

    @Test
    void calculateBreakEvenPoint_ShouldReturnCorrectValue() {
        // Test with valid inputs
        BigDecimal fixedCosts = new BigDecimal("100000.00");
        BigDecimal contributionMargin = new BigDecimal("20.00");
        BigDecimal expected = new BigDecimal("5000.00");
        
        BigDecimal result = calculator.calculateBreakEvenPoint(fixedCosts, contributionMargin);
        
        assertEquals(expected, result);
        
        // Test with zero contribution margin (should return null)
        contributionMargin = BigDecimal.ZERO;
        
        result = calculator.calculateBreakEvenPoint(fixedCosts, contributionMargin);
        
        assertNull(result);
    }
}
