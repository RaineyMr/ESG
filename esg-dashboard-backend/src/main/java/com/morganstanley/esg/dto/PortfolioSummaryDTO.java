package com.morganstanley.esg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Portfolio Summary.
 * 
 * Used for API response to provide a comprehensive summary of portfolio
 * including key metrics, ESG scores, risk ratings, and holdings count.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioSummaryDTO {

    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalValue;
    private String baseCurrency;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime inceptionDate;

    // Portfolio metrics
    private Integer holdingCount;
    private BigDecimal averageEsgScore;
    private String overallRiskLevel;
    private BigDecimal averageBeta;
    private BigDecimal averageVolatility;

    // ESG metrics
    private String esgRating;
    private Integer controversyCount;
    private BigDecimal environmentalScore;
    private BigDecimal socialScore;
    private BigDecimal governanceScore;

    // Risk metrics
    private BigDecimal portfolioBeta;
    private BigDecimal portfolioValueAtRisk;
    private BigDecimal portfolioSharpeRatio;
    private String riskRating;

    // Performance metrics
    private BigDecimal totalUnrealizedGainLoss;
    private BigDecimal totalUnrealizedGainLossPercentage;
    private BigDecimal portfolioConcentration; // Top 10 holdings percentage

    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;

    // Default constructor
    public PortfolioSummaryDTO() {
    }

    // Constructor with required fields
    public PortfolioSummaryDTO(Long portfolioId, String portfolioName, BigDecimal totalValue, 
                              String baseCurrency, LocalDateTime inceptionDate) {
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.totalValue = totalValue;
        this.baseCurrency = baseCurrency;
        this.inceptionDate = inceptionDate;
    }

    // Getters and Setters
    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public LocalDateTime getInceptionDate() {
        return inceptionDate;
    }

    public void setInceptionDate(LocalDateTime inceptionDate) {
        this.inceptionDate = inceptionDate;
    }

    public Integer getHoldingCount() {
        return holdingCount;
    }

    public void setHoldingCount(Integer holdingCount) {
        this.holdingCount = holdingCount;
    }

    public BigDecimal getAverageEsgScore() {
        return averageEsgScore;
    }

    public void setAverageEsgScore(BigDecimal averageEsgScore) {
        this.averageEsgScore = averageEsgScore;
    }

    public String getOverallRiskLevel() {
        return overallRiskLevel;
    }

    public void setOverallRiskLevel(String overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
    }

    public BigDecimal getAverageBeta() {
        return averageBeta;
    }

    public void setAverageBeta(BigDecimal averageBeta) {
        this.averageBeta = averageBeta;
    }

    public BigDecimal getAverageVolatility() {
        return averageVolatility;
    }

    public void setAverageVolatility(BigDecimal averageVolatility) {
        this.averageVolatility = averageVolatility;
    }

    public String getEsgRating() {
        return esgRating;
    }

    public void setEsgRating(String esgRating) {
        this.esgRating = esgRating;
    }

    public Integer getControversyCount() {
        return controversyCount;
    }

    public void setControversyCount(Integer controversyCount) {
        this.controversyCount = controversyCount;
    }

    public BigDecimal getEnvironmentalScore() {
        return environmentalScore;
    }

    public void setEnvironmentalScore(BigDecimal environmentalScore) {
        this.environmentalScore = environmentalScore;
    }

    public BigDecimal getSocialScore() {
        return socialScore;
    }

    public void setSocialScore(BigDecimal socialScore) {
        this.socialScore = socialScore;
    }

    public BigDecimal getGovernanceScore() {
        return governanceScore;
    }

    public void setGovernanceScore(BigDecimal governanceScore) {
        this.governanceScore = governanceScore;
    }

    public BigDecimal getPortfolioBeta() {
        return portfolioBeta;
    }

    public void setPortfolioBeta(BigDecimal portfolioBeta) {
        this.portfolioBeta = portfolioBeta;
    }

    public BigDecimal getPortfolioValueAtRisk() {
        return portfolioValueAtRisk;
    }

    public void setPortfolioValueAtRisk(BigDecimal portfolioValueAtRisk) {
        this.portfolioValueAtRisk = portfolioValueAtRisk;
    }

    public BigDecimal getPortfolioSharpeRatio() {
        return portfolioSharpeRatio;
    }

    public void setPortfolioSharpeRatio(BigDecimal portfolioSharpeRatio) {
        this.portfolioSharpeRatio = portfolioSharpeRatio;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(String riskRating) {
        this.riskRating = riskRating;
    }

    public BigDecimal getTotalUnrealizedGainLoss() {
        return totalUnrealizedGainLoss;
    }

    public void setTotalUnrealizedGainLoss(BigDecimal totalUnrealizedGainLoss) {
        this.totalUnrealizedGainLoss = totalUnrealizedGainLoss;
    }

    public BigDecimal getTotalUnrealizedGainLossPercentage() {
        return totalUnrealizedGainLossPercentage;
    }

    public void setTotalUnrealizedGainLossPercentage(BigDecimal totalUnrealizedGainLossPercentage) {
        this.totalUnrealizedGainLossPercentage = totalUnrealizedGainLossPercentage;
    }

    public BigDecimal getPortfolioConcentration() {
        return portfolioConcentration;
    }

    public void setPortfolioConcentration(BigDecimal portfolioConcentration) {
        this.portfolioConcentration = portfolioConcentration;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Business methods
    public void calculateEsgRating() {
        if (averageEsgScore != null) {
            if (averageEsgScore.compareTo(new BigDecimal("80")) >= 0) {
                this.esgRating = "EXCELLENT";
            } else if (averageEsgScore.compareTo(new BigDecimal("60")) >= 0) {
                this.esgRating = "GOOD";
            } else if (averageEsgScore.compareTo(new BigDecimal("40")) >= 0) {
                this.esgRating = "AVERAGE";
            } else if (averageEsgScore.compareTo(new BigDecimal("20")) >= 0) {
                this.esgRating = "BELOW_AVERAGE";
            } else {
                this.esgRating = "POOR";
            }
        }
    }

    public void calculateOverallRiskLevel() {
        if (averageVolatility != null && averageBeta != null) {
            int riskScore = 0;
            
            if (averageVolatility.compareTo(new BigDecimal("0.40")) > 0) {
                riskScore += 3;
            } else if (averageVolatility.compareTo(new BigDecimal("0.30")) > 0) {
                riskScore += 2;
            } else if (averageVolatility.compareTo(new BigDecimal("0.20")) > 0) {
                riskScore += 1;
            }
            
            if (averageBeta.abs().compareTo(new BigDecimal("2.0")) > 0) {
                riskScore += 3;
            } else if (averageBeta.abs().compareTo(new BigDecimal("1.5")) > 0) {
                riskScore += 2;
            } else if (averageBeta.abs().compareTo(new BigDecimal("1.0")) > 0) {
                riskScore += 1;
            }
            
            if (riskScore >= 5) {
                this.overallRiskLevel = "VERY_HIGH";
            } else if (riskScore >= 4) {
                this.overallRiskLevel = "HIGH";
            } else if (riskScore >= 2) {
                this.overallRiskLevel = "MODERATE";
            } else {
                this.overallRiskLevel = "LOW";
            }
        }
    }

    @Override
    public String toString() {
        return "PortfolioSummaryDTO{" +
                "portfolioId=" + portfolioId +
                ", portfolioName='" + portfolioName + '\'' +
                ", totalValue=" + totalValue +
                ", baseCurrency='" + baseCurrency + '\'' +
                ", holdingCount=" + holdingCount +
                ", averageEsgScore=" + averageEsgScore +
                ", overallRiskLevel='" + overallRiskLevel + '\'' +
                ", averageBeta=" + averageBeta +
                ", averageVolatility=" + averageVolatility +
                '}';
    }
}
