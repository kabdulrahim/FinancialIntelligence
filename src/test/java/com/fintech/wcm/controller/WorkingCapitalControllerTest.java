package com.fintech.wcm.controller;

import com.fintech.wcm.dto.WorkingCapitalMetricsDto;
import com.fintech.wcm.service.WorkingCapitalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the WorkingCapitalController.
 */
@ExtendWith(MockitoExtension.class)
public class WorkingCapitalControllerTest {

    @Mock
    private WorkingCapitalService workingCapitalService;

    @InjectMocks
    private WorkingCapitalController workingCapitalController;

    private final Long companyId = 1L;

    @Test
    void calculateWorkingCapitalMetrics_ShouldReturnMetrics() {
        // Create a WorkingCapitalMetricsDto object
        WorkingCapitalMetricsDto expectedMetrics = WorkingCapitalMetricsDto.builder()
                .companyId(companyId)
                .companyName("Test Company")
                .calculationDate(LocalDate.now())
                .totalCurrentAssets(new BigDecimal("225000.00"))
                .cashAndEquivalents(new BigDecimal("50000.00"))
                .accountsReceivable(new BigDecimal("75000.00"))
                .inventory(new BigDecimal("100000.00"))
                .totalCurrentLiabilities(new BigDecimal("100000.00"))
                .accountsPayable(new BigDecimal("60000.00"))
                .shortTermDebt(new BigDecimal("40000.00"))
                .netWorkingCapital(new BigDecimal("125000.00"))
                .currentRatio(new BigDecimal("2.25"))
                .quickRatio(new BigDecimal("1.25"))
                .build();

        // Mock service method
        when(workingCapitalService.calculateWorkingCapitalMetrics(companyId)).thenReturn(expectedMetrics);

        // Call controller method
        ResponseEntity<WorkingCapitalMetricsDto> response = workingCapitalController.calculateWorkingCapitalMetrics(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMetrics, response.getBody());
    }

    @Test
    void calculateWorkingCapitalMetricsAsOf_ShouldReturnMetrics() {
        // Create a WorkingCapitalMetricsDto object
        LocalDate asOfDate = LocalDate.of(2023, 1, 1);
        WorkingCapitalMetricsDto expectedMetrics = WorkingCapitalMetricsDto.builder()
                .companyId(companyId)
                .companyName("Test Company")
                .calculationDate(asOfDate)
                .totalCurrentAssets(new BigDecimal("225000.00"))
                .cashAndEquivalents(new BigDecimal("50000.00"))
                .accountsReceivable(new BigDecimal("75000.00"))
                .inventory(new BigDecimal("100000.00"))
                .totalCurrentLiabilities(new BigDecimal("100000.00"))
                .accountsPayable(new BigDecimal("60000.00"))
                .shortTermDebt(new BigDecimal("40000.00"))
                .netWorkingCapital(new BigDecimal("125000.00"))
                .currentRatio(new BigDecimal("2.25"))
                .quickRatio(new BigDecimal("1.25"))
                .build();

        // Mock service method
        when(workingCapitalService.calculateWorkingCapitalMetrics(companyId, asOfDate)).thenReturn(expectedMetrics);

        // Call controller method
        ResponseEntity<WorkingCapitalMetricsDto> response = workingCapitalController.calculateWorkingCapitalMetricsAsOf(companyId, asOfDate);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMetrics, response.getBody());
    }

    @Test
    void calculateDSO_ShouldReturnDSO() {
        // Mock service method
        when(workingCapitalService.calculateDSO(companyId)).thenReturn(45.0);

        // Call controller method
        ResponseEntity<Map<String, Double>> response = workingCapitalController.calculateDSO(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(45.0, response.getBody().get("dso"));
    }

    @Test
    void calculateDPO_ShouldReturnDPO() {
        // Mock service method
        when(workingCapitalService.calculateDPO(companyId)).thenReturn(30.0);

        // Call controller method
        ResponseEntity<Map<String, Double>> response = workingCapitalController.calculateDPO(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(30.0, response.getBody().get("dpo"));
    }

    @Test
    void calculateDIO_ShouldReturnDIO() {
        // Mock service method
        when(workingCapitalService.calculateDIO(companyId)).thenReturn(60.0);

        // Call controller method
        ResponseEntity<Map<String, Double>> response = workingCapitalController.calculateDIO(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(60.0, response.getBody().get("dio"));
    }

    @Test
    void calculateCCC_ShouldReturnCCC() {
        // Mock service method
        when(workingCapitalService.calculateCCC(companyId)).thenReturn(75.0);

        // Call controller method
        ResponseEntity<Map<String, Double>> response = workingCapitalController.calculateCCC(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(75.0, response.getBody().get("ccc"));
    }

    @Test
    void calculateLiquidityRatios_ShouldReturnRatios() {
        // Create a map of liquidity ratios
        Map<String, Double> expectedRatios = new HashMap<>();
        expectedRatios.put("currentRatio", 2.25);
        expectedRatios.put("quickRatio", 1.25);
        expectedRatios.put("cashRatio", 0.5);

        // Mock service method
        when(workingCapitalService.calculateLiquidityRatios(companyId)).thenReturn(expectedRatios);

        // Call controller method
        ResponseEntity<Map<String, Double>> response = workingCapitalController.calculateLiquidityRatios(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedRatios, response.getBody());
    }

    @Test
    void generateAlerts_ShouldReturnAlertCount() {
        // Mock service method
        when(workingCapitalService.generateAlerts(companyId)).thenReturn(8);

        // Call controller method
        ResponseEntity<Map<String, Integer>> response = workingCapitalController.generateAlerts(companyId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(8, response.getBody().get("alertsGenerated"));
    }
}
