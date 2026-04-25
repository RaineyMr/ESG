package com.morganstanley.esg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * ESG Metric entity representing portfolio-level ESG scores.
 * 
 * Tracks Environmental, Social, and Governance scores for a portfolio over time.
 * Each metric is dated to allow historical tracking and trend analysis.
 */
@Entity
@Table(name = "esg_metrics", indexes = {
    @Index(name = "idx_esg_metrics_portfolio", columnList = "portfolio_id"),
    @Index(name = "idx_esg_metrics_date", columnList = "metric_date"),
    @UniqueConstraint(name = "uq_portfolio_metric_date", columnNames = {"portfolio_id", "metric_date"})
})
public class EsgMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Portfolio is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_esg_metrics_portfolio"))
    private Portfolio portfolio;

    @NotNull(message = "Metric date is required")
    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @NotNull(message = "Environmental score is required")
    @DecimalMin(value = "0.0", message = "Environmental score must be non-negative")
    @Column(name = "environmental_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal environmentalScore;

    @NotNull(message = "Social score is required")
    @DecimalMin(value = "0.0", message = "Social score must be non-negative")
    @Column(name = "social_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal socialScore;

    @NotNull(message = "Governance score is required")
    @DecimalMin(value = "0.0", message = "Governance score must be non-negative")
    @Column(name = "governance_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal governanceScore;

    @NotNull(message = "Overall ESG score is required")
    @DecimalMin(value = "0.0", message = "Overall ESG score must be non-negative")
    @Column(name = "overall_esg_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallEsgScore;

    @Min(value = 0, message = "Controversy count must be non-negative")
    @Column(name = "controversy_count", nullable = false)
    private Integer controversyCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public EsgMetric() {
        this.metricDate = LocalDate.now();
    }

    // Constructor with required fields
    public EsgMetric(Portfolio portfolio, LocalDate metricDate, 
                    BigDecimal environmentalScore, BigDecimal socialScore, BigDecimal governanceScore) {
        this();
        this.portfolio = portfolio;
        this.metricDate = metricDate;
        this.environmentalScore = environmentalScore;
        this.socialScore = socialScore;
        this.governanceScore = governanceScore;
        calculateOverallScore();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public BigDecimal getEnvironmentalScore() {
        return environmentalScore;
    }

    public void setEnvironmentalScore(BigDecimal environmentalScore) {
        this.environmentalScore = environmentalScore;
        calculateOverallScore();
    }

    public BigDecimal getSocialScore() {
        return socialScore;
    }

    public void setSocialScore(BigDecimal socialScore) {
        this.socialScore = socialScore;
        calculateOverallScore();
    }

    public BigDecimal getGovernanceScore() {
        return governanceScore;
    }

    public void setGovernanceScore(BigDecimal governanceScore) {
        this.governanceScore = governanceScore;
        calculateOverallScore();
    }

    public BigDecimal getOverallEsgScore() {
        return overallEsgScore;
    }

    public void setOverallEsgScore(BigDecimal overallEsgScore) {
        this.overallEsgScore = overallEsgScore;
    }

    public Integer getControversyCount() {
        return controversyCount;
    }

    public void setControversyCount(Integer controversyCount) {
        this.controversyCount = controversyCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    private void calculateOverallScore() {
        if (environmentalScore != null && socialScore != null && governanceScore != null) {
            // Simple average of the three pillars
            this.overallEsgScore = environmentalScore
                    .add(socialScore)
                    .add(governanceScore)
                    .divide(new BigDecimal("3"), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public String getEsgRating() {
        if (overallEsgScore == null) {
            return "NOT_RATED";
        }
        
        if (overallEsgScore.compareTo(new BigDecimal("80")) >= 0) {
            return "EXCELLENT";
        } else if (overallEsgScore.compareTo(new BigDecimal("60")) >= 0) {
            return "GOOD";
        } else if (overallEsgScore.compareTo(new BigDecimal("40")) >= 0) {
            return "AVERAGE";
        } else if (overallEsgScore.compareTo(new BigDecimal("20")) >= 0) {
            return "BELOW_AVERAGE";
        } else {
            return "POOR";
        }
    }

    public String getControversyLevel() {
        if (controversyCount == null) {
            return "UNKNOWN";
        }
        
        if (controversyCount == 0) {
            return "NONE";
        } else if (controversyCount <= 2) {
            return "LOW";
        } else if (controversyCount <= 5) {
            return "MODERATE";
        } else if (controversyCount <= 10) {
            return "HIGH";
        } else {
            return "VERY_HIGH";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EsgMetric that = (EsgMetric) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EsgMetric{" +
                "id=" + id +
                ", metricDate=" + metricDate +
                ", environmentalScore=" + environmentalScore +
                ", socialScore=" + socialScore +
                ", governanceScore=" + governanceScore +
                ", overallEsgScore=" + overallEsgScore +
                ", controversyCount=" + controversyCount +
                '}';
    }
}
