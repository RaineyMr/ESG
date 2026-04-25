package com.morganstanley.esg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * ESG Score entity representing company-level ESG ratings.
 * 
 * Tracks detailed ESG scores for individual holdings (companies) over time.
 * Each score includes the three ESG pillars and controversy level.
 */
@Entity
@Table(name = "esg_scores", indexes = {
    @Index(name = "idx_esg_scores_holding", columnList = "holding_id"),
    @Index(name = "idx_esg_scores_date", columnList = "score_date"),
    @Index(name = "idx_esg_scores_overall", columnList = "overall_score"),
    @UniqueConstraint(name = "uq_holding_score_date", columnNames = {"holding_id", "score_date"})
})
public class EsgScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Holding is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holding_id", nullable = false, foreignKey = @ForeignKey(name = "fk_esg_scores_holding"))
    private Holding holding;

    @NotNull(message = "Score date is required")
    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    @NotNull(message = "Overall score is required")
    @DecimalMin(value = "0.0", message = "Overall score must be non-negative")
    @Column(name = "overall_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallScore;

    @NotNull(message = "Environmental pillar score is required")
    @DecimalMin(value = "0.0", message = "Environmental pillar score must be non-negative")
    @Column(name = "environmental_pillar", nullable = false, precision = 5, scale = 2)
    private BigDecimal environmentalPillar;

    @NotNull(message = "Social pillar score is required")
    @DecimalMin(value = "0.0", message = "Social pillar score must be non-negative")
    @Column(name = "social_pillar", nullable = false, precision = 5, scale = 2)
    private BigDecimal socialPillar;

    @NotNull(message = "Governance pillar score is required")
    @DecimalMin(value = "0.0", message = "Governance pillar score must be non-negative")
    @Column(name = "governance_pillar", nullable = false, precision = 5, scale = 2)
    private BigDecimal governancePillar;

    @NotNull(message = "Controversy level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "controversy_level", nullable = false, length = 20)
    private ControversyLevel controversyLevel;

    @Size(max = 100, message = "Data provider must not exceed 100 characters")
    @Column(name = "data_provider", length = 100)
    private String dataProvider;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum for controversy levels
    public enum ControversyLevel {
        LOW, MODERATE, HIGH, VERY_HIGH
    }

    // Default constructor
    public EsgScore() {
        this.scoreDate = LocalDate.now();
        this.controversyLevel = ControversyLevel.LOW;
    }

    // Constructor with required fields
    public EsgScore(Holding holding, LocalDate scoreDate, 
                   BigDecimal overallScore, BigDecimal environmentalPillar, 
                   BigDecimal socialPillar, BigDecimal governancePillar, 
                   ControversyLevel controversyLevel) {
        this();
        this.holding = holding;
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

    public Holding getHolding() {
        return holding;
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
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

    public ControversyLevel getControversyLevel() {
        return controversyLevel;
    }

    public void setControversyLevel(ControversyLevel controversyLevel) {
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

    // Business methods
    public String getEsgRating() {
        if (overallScore == null) {
            return "NOT_RATED";
        }
        
        if (overallScore.compareTo(new BigDecimal("80")) >= 0) {
            return "AAA";
        } else if (overallScore.compareTo(new BigDecimal("70")) >= 0) {
            return "AA";
        } else if (overallScore.compareTo(new BigDecimal("60")) >= 0) {
            return "A";
        } else if (overallScore.compareTo(new BigDecimal("50")) >= 0) {
            return "BBB";
        } else if (overallScore.compareTo(new BigDecimal("40")) >= 0) {
            return "BB";
        } else if (overallScore.compareTo(new BigDecimal("30")) >= 0) {
            return "B";
        } else if (overallScore.compareTo(new BigDecimal("20")) >= 0) {
            return "CCC";
        } else {
            return "D";
        }
    }

    public BigDecimal getPillarAverage() {
        if (environmentalPillar != null && socialPillar != null && governancePillar != null) {
            return environmentalPillar
                    .add(socialPillar)
                    .add(governancePillar)
                    .divide(new BigDecimal("3"), 2, BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    public boolean isValidScoreRange() {
        return overallScore != null && overallScore.compareTo(BigDecimal.ZERO) >= 0 && overallScore.compareTo(new BigDecimal("100")) <= 0 &&
               environmentalPillar != null && environmentalPillar.compareTo(BigDecimal.ZERO) >= 0 && environmentalPillar.compareTo(new BigDecimal("100")) <= 0 &&
               socialPillar != null && socialPillar.compareTo(BigDecimal.ZERO) >= 0 && socialPillar.compareTo(new BigDecimal("100")) <= 0 &&
               governancePillar != null && governancePillar.compareTo(BigDecimal.ZERO) >= 0 && governancePillar.compareTo(new BigDecimal("100")) <= 0;
    }

    public BigDecimal getControversyScore() {
        switch (controversyLevel) {
            case LOW:
                return new BigDecimal("10");
            case MODERATE:
                return new BigDecimal("20");
            case HIGH:
                return new BigDecimal("30");
            case VERY_HIGH:
                return new BigDecimal("40");
            default:
                return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EsgScore esgScore = (EsgScore) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EsgScore{" +
                "id=" + id +
                ", scoreDate=" + scoreDate +
                ", overallScore=" + overallScore +
                ", environmentalPillar=" + environmentalPillar +
                ", socialPillar=" + socialPillar +
                ", governancePillar=" + governancePillar +
                ", controversyLevel=" + controversyLevel +
                ", dataProvider='" + dataProvider + '\'' +
                '}';
    }
}
