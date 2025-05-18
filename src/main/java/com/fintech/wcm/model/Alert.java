package com.fintech.wcm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a system alert to users about working capital issues.
 */
@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "is_dismissed", nullable = false)
    private boolean dismissed = false;

    @Column(name = "trigger_metric")
    private String triggerMetric;

    @Column(name = "trigger_threshold")
    private String triggerThreshold;

    @Column(name = "trigger_value")
    private String triggerValue;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    /**
     * Enum representing alert types.
     */
    public enum AlertType {
        CASH_GAP,
        LIQUIDITY_ISSUE,
        OVERDUE_RECEIVABLE,
        OVERDUE_PAYABLE,
        LOW_INVENTORY,
        CASH_FLOW_FORECAST,
        WORKING_CAPITAL_RATIO,
        QUICK_RATIO,
        CCC_ISSUE,
        SYSTEM_NOTIFICATION
    }

    /**
     * Enum representing alert severity levels.
     */
    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
