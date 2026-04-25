package com.morganstanley.esg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for EsgScore entity.
 * 
 * Used for API request/response to transfer ESG score data between
 * client and server while maintaining proper validation and serialization.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EsgScoreDTO {

    private Long id;
    private Long holdingId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Score date is required")
    private LocalDate scoreDate;

    @NotNull(message = "Overall score is required")
    @DecimalMin(value = "0.0", message = "Overall score must be non-negative")
    private BigDecimal overallScore;

    @NotNull(message = "Environmental pillar score is required")
    @DecimalMin(value = "0.0", message = "Environmental pillar score must be non-negative")
    private BigDecimal environmentalPillar;

    @NotNull(message = "Social pillar score is required")
    @DecimalMin(value = "0.0", message = "Social pillar score must be non-negative")
    private BigDecimal socialPillar;

    @NotNull(message = "Governance pillar score is required")
    @DecimalMin(value = "0.0", message = "Governance pillar score must be non-negative")
    private BigDecimal governancePillar;

    @NotNull(message = "Controversy level is required")
    private String controversyLevel;

    @Size(max = 100, message = "Data provider must not exceed 100 characters")
    private String dataProvider;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Calculated fields
    private String esgRating;
    private BigDecimal pillarAverage;

    // Default constructor
    public EsgScoreDTO() {
        this.controversyLevel = "LOW";
    }

    // Constructor with required fields
    public EsgScoreDTO(LocalDate scoreDate, BigDecimal overallScore, 
                      BigDecimal environmentalPillar, BigDecimal socialPillar, 
                      BigDecimal governancePillar, String controversyLevel) {
        this();
        this.scoreDate = scoreDate;
        this.overallScore = overallScore;
        this.environmentalPillar = environmentalPillar;
        this.socialPillar = socialPillar;
        this.governancePillar = governancePillar;
        this.controversyLevel = controversyLevel;
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

    public LocalDate getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(LocalDate scoreDate) {
        this.scoreDate = scoreDate;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public BigDecimal getEnvironmentalPillar() {
        return environmentalPillar;
    }

    public void setEnvironmentalPillar(BigDecimal environmentalPillar) {
        this.environmentalPillar = environmentalPillar;
    }

    public BigDecimal getSocialPillar() {
        return socialPillar;
    }

    public void setSocialPillar(BigDecimal socialPillar) {
        this.socialPillar = socialPillar;
    }

    public BigDecimal getGovernancePillar() {
        return governancePillar;
    }

    public void setGovernancePillar(BigDecimal governancePillar) {
        this.governancePillar = governancePillar;
    }

    public String getControversyLevel() {
        return controversyLevel;
    }

    public void setControversyLevel(String controversyLevel) {
        this.controversyLevel = controversyLevel;
    }

    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
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

    public BigDecimal getPillarAverage() {
        return pillarAverage;
    }

    public void setPillarAverage(BigDecimal pillarAverage) {
        this.pillarAverage = pillarAverage;
    }

    @Override
    public String toString() {
        return "EsgScoreDTO{" +
                "id=" + id +
                ", scoreDate=" + scoreDate +
                ", overallScore=" + overallScore +
                ", environmentalPillar=" + environmentalPillar +
                ", socialPillar=" + socialPillar +
                ", governancePillar=" + governancePillar +
                ", controversyLevel='" + controversyLevel + '\'' +
                ", dataProvider='" + dataProvider + '\'' +
                '}';
    }
}
