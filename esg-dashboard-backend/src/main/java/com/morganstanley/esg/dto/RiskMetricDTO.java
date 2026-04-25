package com.morganstanley.esg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for RiskMetric entity.
 * 
 * Used for API request/response to transfer risk metric data between
 * client and server while maintaining proper validation and serialization.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskMetricDTO {

    private Long id;
    private Long holdingId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Calculation date is required")
    private LocalDate calculationDate;

    private BigDecimal beta;
    private BigDecimal volatility;
    private BigDecimal valueAtRisk;
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;

    @NotNull(message = "Risk rating is required")
    private String riskRating;

    @Min(value = 1, message = "Time horizon must be at least 1 day")
    private Integer timeHorizonDays = 252;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Calculated fields
    private String riskLevelDescription;
    private BigDecimal annualizedVolatility;
    private boolean isHighBeta;
    private boolean isHighVolatility;
    private boolean hasNegativeSharpeRatio;

    // Default constructor
    public RiskMetricDTO() {
        this.riskRating = "MODERATE";
        this.timeHorizonDays = 252;
    }

    // Constructor with required fields
    public RiskMetricDTO(LocalDate calculationDate, String riskRating) {
        this();
        this.calculationDate = calculationDate;
        this.riskRating = riskRating;
    }

    // Constructor with all fields
    public RiskMetricDTO(LocalDate calculationDate, BigDecimal beta, BigDecimal volatility, 
                         BigDecimal valueAtRisk, BigDecimal maxDrawdown, BigDecimal sharpeRatio, 
                         String riskRating, Integer timeHorizonDays) {
        this(calculationDate, riskRating);
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

    public Long getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(Long holdingId) {
        this.holdingId = holdingId;
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
        calculateRiskFlags();
    }

    public BigDecimal getVolatility() {
        return volatility;
    }

    public void setVolatility(BigDecimal volatility) {
        this.volatility = volatility;
        calculateAnnualizedVolatility();
        calculateRiskFlags();
    }

    public BigDecimal getValueAtRisk() {
        return valueAtRisk;
    }

    public void setValueAtRisk(BigDecimal valueAtRisk) {
        this.valueAtRisk = valueAtRisk;
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(BigDecimal maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(BigDecimal sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
        calculateRiskFlags();
    }

    public String getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(String riskRating) {
        this.riskRating = riskRating;
        calculateRiskDescription();
    }

    public Integer getTimeHorizonDays() {
        return timeHorizonDays;
    }

    public void setTimeHorizonDays(Integer timeHorizonDays) {
        this.timeHorizonDays = timeHorizonDays;
        calculateAnnualizedVolatility();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRiskLevelDescription() {
        return riskLevelDescription;
    }

    public void setRiskLevelDescription(String riskLevelDescription) {
        this.riskLevelDescription = riskLevelDescription;
    }

    public BigDecimal getAnnualizedVolatility() {
        return annualizedVolatility;
    }

    public void setAnnualizedVolatility(BigDecimal annualizedVolatility) {
        this.annualizedVolatility = annualizedVolatility;
    }

    public boolean isHighBeta() {
        return isHighBeta;
    }

    public void setHighBeta(boolean highBeta) {
        isHighBeta = highBeta;
    }

    public boolean isHighVolatility() {
        return isHighVolatility;
    }

    public void setHighVolatility(boolean highVolatility) {
        isHighVolatility = highVolatility;
    }

    public boolean hasNegativeSharpeRatio() {
        return hasNegativeSharpeRatio;
    }

    public void setNegativeSharpeRatio(boolean negativeSharpeRatio) {
        hasNegativeSharpeRatio = negativeSharpeRatio;
    }

    // Business methods
    private void calculateRiskDescription() {
        switch (riskRating) {
            case "LOW":
                this.riskLevelDescription = "Low risk - suitable for conservative investors";
                break;
            case "MODERATE":
                this.riskLevelDescription = "Moderate risk - balanced risk-return profile";
                break;
            case "HIGH":
                this.riskLevelDescription = "High risk - suitable for aggressive investors";
                break;
            case "VERY_HIGH":
                this.riskLevelDescription = "Very high risk - speculative investment";
                break;
            default:
                this.riskLevelDescription = "Unknown risk level";
                break;
        }
    }

    private void calculateAnnualizedVolatility() {
        if (volatility != null && timeHorizonDays != null && timeHorizonDays > 0) {
            // Convert to annualized volatility
            BigDecimal sqrtFactor = new BigDecimal(Math.sqrt(252.0 / timeHorizonDays));
            this.annualizedVolatility = volatility.multiply(sqrtFactor);
        }
    }

    private void calculateRiskFlags() {
        this.isHighBeta = beta != null && beta.abs().compareTo(new BigDecimal("1.5")) > 0;
        this.isHighVolatility = volatility != null && volatility.compareTo(new BigDecimal("0.30")) > 0;
        this.hasNegativeSharpeRatio = sharpeRatio != null && sharpeRatio.compareTo(BigDecimal.ZERO) < 0;
    }

    public void calculateAllFields() {
        calculateRiskDescription();
        calculateAnnualizedVolatility();
        calculateRiskFlags();
    }

    @Override
    public String toString() {
        return "RiskMetricDTO{" +
                "id=" + id +
                ", calculationDate=" + calculationDate +
                ", beta=" + beta +
                ", volatility=" + volatility +
                ", valueAtRisk=" + valueAtRisk +
                ", maxDrawdown=" + maxDrawdown +
                ", sharpeRatio=" + sharpeRatio +
                ", riskRating='" + riskRating + '\'' +
                ", timeHorizonDays=" + timeHorizonDays +
                '}';
    }
}
