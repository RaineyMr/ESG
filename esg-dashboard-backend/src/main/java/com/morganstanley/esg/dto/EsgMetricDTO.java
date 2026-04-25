package com.morganstanley.esg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for EsgMetric entity.
 * 
 * Used for API request/response to transfer portfolio-level ESG metric data between
 * client and server while maintaining proper validation and serialization.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EsgMetricDTO {

    private Long id;
    private Long portfolioId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Metric date is required")
    private LocalDate metricDate;

    @NotNull(message = "Environmental score is required")
    @DecimalMin(value = "0.0", message = "Environmental score must be non-negative")
    private BigDecimal environmentalScore;

    @NotNull(message = "Social score is required")
    @DecimalMin(value = "0.0", message = "Social score must be non-negative")
    private BigDecimal socialScore;

    @NotNull(message = "Governance score is required")
    @DecimalMin(value = "0.0", message = "Governance score must be non-negative")
    private BigDecimal governanceScore;

    @NotNull(message = "Overall ESG score is required")
    @DecimalMin(value = "0.0", message = "Overall ESG score must be non-negative")
    private BigDecimal overallEsgScore;

    @Min(value = 0, message = "Controversy count must be non-negative")
    private Integer controversyCount = 0;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Calculated fields
    private String esgRating;
    private String controversyLevel;

    // Default constructor
    public EsgMetricDTO() {
        this.controversyCount = 0;
    }

    // Constructor with required fields
    public EsgMetricDTO(LocalDate metricDate, BigDecimal environmentalScore, 
                        BigDecimal socialScore, BigDecimal governanceScore, 
                        Integer controversyCount) {
        this();
        this.metricDate = metricDate;
        this.environmentalScore = environmentalScore;
        this.socialScore = socialScore;
        this.governanceScore = governanceScore;
        this.controversyCount = controversyCount;
        calculateOverallScore();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
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

    public String getEsgRating() {
        return esgRating;
    }

    public void setEsgRating(String esgRating) {
        this.esgRating = esgRating;
    }

    public String getControversyLevel() {
        return controversyLevel;
    }

    public void setControversyLevel(String controversyLevel) {
        this.controversyLevel = controversyLevel;
    }

    // Business methods
    private void calculateOverallScore() {
        if (environmentalScore != null && socialScore != null && governanceScore != null) {
            this.overallEsgScore = environmentalScore
                    .add(socialScore)
                    .add(governanceScore)
                    .divide(new BigDecimal("3"), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public void calculateRatings() {
        // Calculate ESG rating
        if (overallEsgScore != null) {
            if (overallEsgScore.compareTo(new BigDecimal("80")) >= 0) {
                this.esgRating = "EXCELLENT";
            } else if (overallEsgScore.compareTo(new BigDecimal("60")) >= 0) {
                this.esgRating = "GOOD";
            } else if (overallEsgScore.compareTo(new BigDecimal("40")) >= 0) {
                this.esgRating = "AVERAGE";
            } else if (overallEsgScore.compareTo(new BigDecimal("20")) >= 0) {
                this.esgRating = "BELOW_AVERAGE";
            } else {
                this.esgRating = "POOR";
            }
        }

        // Calculate controversy level
        if (controversyCount != null) {
            if (controversyCount == 0) {
                this.controversyLevel = "NONE";
            } else if (controversyCount <= 2) {
                this.controversyLevel = "LOW";
            } else if (controversyCount <= 5) {
                this.controversyLevel = "MODERATE";
            } else if (controversyCount <= 10) {
                this.controversyLevel = "HIGH";
            } else {
                this.controversyLevel = "VERY_HIGH";
            }
        }
    }

    @Override
    public String toString() {
        return "EsgMetricDTO{" +
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
