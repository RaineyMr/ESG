package com.morganstanley.esg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Portfolio entity representing an investment portfolio.
 * 
 * A portfolio contains holdings (securities) and tracks ESG metrics at the portfolio level.
 * Each portfolio has a base currency and tracks its total market value.
 */
@Entity
@Table(name = "portfolios", indexes = {
    @Index(name = "idx_portfolios_name", columnList = "portfolio_name"),
    @Index(name = "idx_portfolios_created", columnList = "created_at")
})
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Portfolio name is required")
    @Size(max = 255, message = "Portfolio name must not exceed 255 characters")
    @Column(name = "portfolio_name", nullable = false, length = 255)
    private String portfolioName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0", message = "Total value must be non-negative")
    @Column(name = "total_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @NotBlank(message = "Base currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency = "USD";

    @Column(name = "inception_date", nullable = false)
    private LocalDateTime inceptionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Holding> holdings = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EsgMetric> esgMetrics = new ArrayList<>();

    // Default constructor
    public Portfolio() {
        this.inceptionDate = LocalDateTime.now();
    }

    // Constructor with required fields
    public Portfolio(String portfolioName, BigDecimal totalValue, String baseCurrency) {
        this();
        this.portfolioName = portfolioName;
        this.totalValue = totalValue;
        this.baseCurrency = baseCurrency;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    public List<EsgMetric> getEsgMetrics() {
        return esgMetrics;
    }

    public void setEsgMetrics(List<EsgMetric> esgMetrics) {
        this.esgMetrics = esgMetrics;
    }

    // Business methods
    public void addHolding(Holding holding) {
        holdings.add(holding);
        holding.setPortfolio(this);
        updateTotalValue();
    }

    public void removeHolding(Holding holding) {
        holdings.remove(holding);
        holding.setPortfolio(null);
        updateTotalValue();
    }

    public void updateTotalValue() {
        BigDecimal total = holdings.stream()
            .map(Holding::getMarketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalValue = total;
    }

    public int getHoldingCount() {
        return holdings.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio portfolio = (Portfolio) o;
        return Objects.equals(id, portfolio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "id=" + id +
                ", portfolioName='" + portfolioName + '\'' +
                ", totalValue=" + totalValue +
                ", baseCurrency='" + baseCurrency + '\'' +
                ", inceptionDate=" + inceptionDate +
                '}';
    }
}
