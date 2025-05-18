package com.fintech.wcm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Invoice entities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceDto {
    
    private Long id;
    private Long companyId;
    private String invoiceNumber;
    private String invoiceType;  // SALES or PURCHASE
    private String contactName;
    private String contactEmail;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmountBaseCurrency;
    private String paymentTerms;
    private String notes;
    private String status;
    private List<PaymentDto> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDto {
        private Long id;
        private LocalDate paymentDate;
        private BigDecimal amount;
        private String currencyCode;
        private String paymentMethod;
        private String referenceNumber;
    }
}
