package com.fintech.wcm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a short-term liability for a company.
 */
@Entity
@Table(name = "short_term_liabilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShortTermLiability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "liability_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LiabilityType liabilityType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "amount_base_currency", precision = 19, scale = 4)
    private BigDecimal amountBaseCurrency;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "creditor")
    private String creditor;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LiabilityStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing short-term liability types.
     */
    public enum LiabilityType {
        SHORT_TERM_LOAN,
        LINE_OF_CREDIT,
        CREDIT_CARD,
        TAX_PAYABLE,
        WAGES_PAYABLE,
        DEFERRED_REVENUE,
        INTEREST_PAYABLE,
        OTHER
    }

    /**
     * Enum representing liability status.
     */
    public enum LiabilityStatus {
        ACTIVE,
        PAID,
        OVERDUE,
        DISPUTED,
        RENEGOTIATED
    }
}
