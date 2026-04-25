package com.morganstanley.esg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Risk Metric entity representing risk analysis data for holdings.
 * 
 * Tracks various risk measures including beta, volatility, Value at Risk (VaR),
 * maximum drawdown, and Sharpe ratio for individual securities over time.
 */
@Entity
@Table(name = "risk_metrics", indexes = {
    @Index(name = "idx_risk_metrics_holding", columnList = "holding_id"),
    @Index(name = "idx_risk_metrics_date", columnList = "calculation_date"),
    @Index(name = "idx_risk_metrics_rating", columnList = "risk_rating"),
    @UniqueConstraint(name = "uq_holding_risk_date", columnNames = {"holding_id", "calculation_date"})
})
public class RiskMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Holding is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holding_id", nullable = false, foreignKey = @ForeignKey(name = "fk_risk_metrics_holding"))
    private Holding holding;

    @NotNull(message = "Calculation date is required")
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    @Column(name = "beta", precision = 8, scale = 4)
    private BigDecimal beta;

    @Column(name = "volatility", precision = 8, scale = 4)
    private BigDecimal volatility;

    @Column(name = "value_at_risk", precision = 8, scale = 4)
    private BigDecimal valueAtRisk;

    @Column(name = "max_drawdown", precision = 8, scale = 4)
    private BigDecimal maxDrawdown;

    @Column(name = "sharpe_ratio", precision = 8, scale = 4)
    private BigDecimal sharpeRatio;

    @NotNull(message = "Risk rating is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_rating", nullable = false, length = 20)
    private RiskRating riskRating;

    @Min(value = 1, message = "Time horizon must be at least 1 day")
    @Column(name = "time_horizon_days", nullable = false)
    private Integer timeHorizonDays = 252; // Default to trading days in a year

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum for risk ratings
    public enum RiskRating {
        LOW, MODERATE, HIGH, VERY_HIGH
    }

    // Default constructor
    public RiskMetric() {
        this.calculationDate = LocalDate.now();
        this.riskRating = RiskRating.MODERATE;
        this.timeHorizonDays = 252;
    }

    // Constructor with required fields
    public RiskMetric(Holding holding, LocalDate calculationDate, RiskRating riskRating) {
        this();
        this.holding = holding;
        this.calculationDate = calculationDate;
        this.riskRating = riskRating;
    }

    // Constructor with all fields
    public RiskMetric(Holding holding, LocalDate calculationDate, 
                     BigDecimal beta, BigDecimal volatility, BigDecimal valueAtRisk,
                     BigDecimal maxDrawdown, BigDecimal sharpeRatio, 
                     RiskRating riskRating, Integer timeHorizonDays) {
        this(holding, calculationDate, riskRating);
        this.beta = beta;
        this.volatility = volatility;
        this.valueAtRisk = valueAtRisk;
        this.maxDrawdown = maxDrawdown;
        this.sharpeRatio = sharpeRatio;
        this.timeHorizonDays = timeHorizonDays;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Holding getHolding() {
        return holding;
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public BigDecimal getBeta() {
        return beta;
    }

    public void setBeta(BigDecimal beta) {
        this.beta = beta;
        updateRiskRating();
    }

    public BigDecimal getVolatility() {
        return volatility;
    }

    public void setVolatility(BigDecimal volatility) {
        this.volatility = volatility;
        updateRiskRating();
    }

    public BigDecimal getValueAtRisk() {
        return valueAtRisk;
    }

    public void setValueAtRisk(BigDecimal valueAtRisk) {
        this.valueAtRisk = valueAtRisk;
        updateRiskRating();
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
        updateRiskRating();
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(BigDecimal sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
        updateRiskRating();
    }

    public RiskRating getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(RiskRating riskRating) {
        this.riskRating = riskRating;
    }

    public Integer getTimeHorizonDays() {
        return timeHorizonDays;
    }

    public void setTimeHorizonDays(Integer timeHorizonDays) {
        this.timeHorizonDays = timeHorizonDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    private void updateRiskRating() {
        int riskScore = 0;
        
        // Beta contribution to risk score
        if (beta != null) {
            if (beta.abs().compareTo(new BigDecimal("2.0")) > 0) {
                riskScore += 3;
            } else if (beta.abs().compareTo(new BigDecimal("1.5")) > 0) {
                riskScore += 2;
            } else if (beta.abs().compareTo(new BigDecimal("1.0")) > 0) {
                riskScore += 1;
            }
        }
        
        // Volatility contribution to risk score
        if (volatility != null) {
            if (volatility.compareTo(new BigDecimal("0.40")) > 0) {
                riskScore += 3;
            } else if (volatility.compareTo(new BigDecimal("0.30")) > 0) {
                riskScore += 2;
            } else if (volatility.compareTo(new BigDecimal("0.20")) > 0) {
                riskScore += 1;
            }
        }
        
        // VaR contribution to risk score
        if (valueAtRisk != null) {
            if (valueAtRisk.compareTo(new BigDecimal("-5.0")) < 0) {
                riskScore += 3;
            } else if (valueAtRisk.compareTo(new BigDecimal("-3.0")) < 0) {
                riskScore += 2;
            } else if (valueAtRisk.compareTo(new BigDecimal("-2.0")) < 0) {
                riskScore += 1;
            }
        }
        
        // Max drawdown contribution to risk score
        if (maxDrawdown != null) {
            if (maxDrawdown.compareTo(new BigDecimal("-30.0")) < 0) {
                riskScore += 3;
            } else if (maxDrawdown.compareTo(new BigDecimal("-20.0")) < 0) {
                riskScore += 2;
            } else if (maxDrawdown.compareTo(new BigDecimal("-10.0")) < 0) {
                riskScore += 1;
            }
        }
        
        // Sharpe ratio contribution (negative Sharpe ratio increases risk)
        if (sharpeRatio != null && sharpeRatio.compareTo(BigDecimal.ZERO) < 0) {
            riskScore += 2;
        }
        
        // Determine risk rating based on total score
        if (riskScore >= 10) {
            this.riskRating = RiskRating.VERY_HIGH;
        } else if (riskScore >= 7) {
            this.riskRating = RiskRating.HIGH;
        } else if (riskScore >= 4) {
            this.riskRating = RiskRating.MODERATE;
        } else {
            this.riskRating = RiskRating.LOW;
        }
    }

    public String getRiskLevelDescription() {
        switch (riskRating) {
            case LOW:
                return "Low risk - suitable for conservative investors";
            case MODERATE:
                return "Moderate risk - balanced risk-return profile";
            case HIGH:
                return "High risk - suitable for aggressive investors";
            case VERY_HIGH:
                return "Very high risk - speculative investment";
            default:
                return "Unknown risk level";
        }
    }

    public BigDecimal getAnnualizedVolatility() {
        if (volatility != null && timeHorizonDays != null && timeHorizonDays > 0) {
            // Convert to annualized volatility
            BigDecimal sqrtFactor = new BigDecimal(Math.sqrt(252.0 / timeHorizonDays));
            return volatility.multiply(sqrtFactor);
        }
        return volatility;
    }

    public boolean isHighBeta() {
        return beta != null && beta.abs().compareTo(new BigDecimal("1.5")) > 0;
    }

    public boolean isHighVolatility() {
        return volatility != null && volatility.compareTo(new BigDecimal("0.30")) > 0;
    }

    public boolean hasNegativeSharpeRatio() {
        return sharpeRatio != null && sharpeRatio.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskMetric that = (RiskMetric) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RiskMetric{" +
                "id=" + id +
                ", calculationDate=" + calculationDate +
                ", beta=" + beta +
                ", volatility=" + volatility +
                ", valueAtRisk=" + valueAtRisk +
                ", maxDrawdown=" + maxDrawdown +
                ", sharpeRatio=" + sharpeRatio +
                ", riskRating=" + riskRating +
                ", timeHorizonDays=" + timeHorizonDays +
                '}';
    }
}
