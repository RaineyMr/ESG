package com.morganstanley.esg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holding entity representing a security (stock, bond, etc.) within a portfolio.
 * 
 * Each holding tracks quantity, prices, market value, and weight in the portfolio.
 * Holdings can have multiple ESG scores and risk metrics over time.
 */
@Entity
@Table(name = "holdings", indexes = {
    @Index(name = "idx_holdings_portfolio", columnList = "portfolio_id"),
    @Index(name = "idx_holdings_ticker", columnList = "ticker_symbol"),
    @Index(name = "idx_holdings_sector", columnList = "sector")
})
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Portfolio is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_holdings_portfolio"))
    private Portfolio portfolio;

    @NotBlank(message = "Ticker symbol is required")
    @Size(max = 10, message = "Ticker symbol must not exceed 10 characters")
    @Column(name = "ticker_symbol", nullable = false, length = 10)
    private String tickerSymbol;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @NotBlank(message = "Sector is required")
    @Size(max = 100, message = "Sector must not exceed 100 characters")
    @Column(name = "sector", nullable = false, length = 100)
    private String sector;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0001", message = "Purchase price must be positive")
    @Column(name = "purchase_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal purchasePrice;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0001", message = "Current price must be positive")
    @Column(name = "current_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal currentPrice;

    @NotNull(message = "Market value is required")
    @DecimalMin(value = "0.0", message = "Market value must be non-negative")
    @Column(name = "market_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal marketValue;

    @NotNull(message = "Weight in portfolio is required")
    @Column(name = "weight_in_portfolio", nullable = false, precision = 5, scale = 4)
    private BigDecimal weightInPortfolio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "holding", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EsgScore> esgScores = new ArrayList<>();

    @OneToMany(mappedBy = "holding", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RiskMetric> riskMetrics = new ArrayList<>();

    // Default constructor
    public Holding() {
    }

    // Constructor with required fields
    public Holding(Portfolio portfolio, String tickerSymbol, String companyName, String sector, 
                   BigDecimal quantity, BigDecimal purchasePrice, BigDecimal currentPrice) {
        this.portfolio = portfolio;
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.sector = sector;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.marketValue = currentPrice.multiply(quantity);
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

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        updateMarketValue();
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        updateMarketValue();
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
        updateWeightInPortfolio();
    }

    public BigDecimal getWeightInPortfolio() {
        return weightInPortfolio;
    }

    public void setWeightInPortfolio(BigDecimal weightInPortfolio) {
        this.weightInPortfolio = weightInPortfolio;
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

    public List<EsgScore> getEsgScores() {
        return esgScores;
    }

    public void setEsgScores(List<EsgScore> esgScores) {
        this.esgScores = esgScores;
    }

    public List<RiskMetric> getRiskMetrics() {
        return riskMetrics;
    }

    public void setRiskMetrics(List<RiskMetric> riskMetrics) {
        this.riskMetrics = riskMetrics;
    }

    // Business methods
    private void updateMarketValue() {
        if (quantity != null && currentPrice != null) {
            this.marketValue = quantity.multiply(currentPrice);
            updateWeightInPortfolio();
        }
    }

    private void updateWeightInPortfolio() {
        if (portfolio != null && portfolio.getTotalValue() != null && portfolio.getTotalValue().compareTo(BigDecimal.ZERO) > 0) {
            this.weightInPortfolio = marketValue.divide(portfolio.getTotalValue(), 4, BigDecimal.ROUND_HALF_UP);
        } else {
            this.weightInPortfolio = BigDecimal.ZERO;
        }
    }

    public void addEsgScore(EsgScore esgScore) {
        esgScores.add(esgScore);
        esgScore.setHolding(this);
    }

    public void removeEsgScore(EsgScore esgScore) {
        esgScores.remove(esgScore);
        esgScore.setHolding(null);
    }

    public void addRiskMetric(RiskMetric riskMetric) {
        riskMetrics.add(riskMetric);
        riskMetric.setHolding(this);
    }

    public void removeRiskMetric(RiskMetric riskMetric) {
        riskMetrics.remove(riskMetric);
        riskMetric.setHolding(null);
    }

    public BigDecimal getUnrealizedGainLoss() {
        if (purchasePrice != null && currentPrice != null && quantity != null) {
            return currentPrice.subtract(purchasePrice).multiply(quantity);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getUnrealizedGainLossPercentage() {
        if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0 && currentPrice != null) {
            return currentPrice.subtract(purchasePrice)
                    .divide(purchasePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holding holding = (Holding) o;
        return Objects.equals(id, holding.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Holding{" +
                "id=" + id +
                ", tickerSymbol='" + tickerSymbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", sector='" + sector + '\'' +
                ", quantity=" + quantity +
                ", currentPrice=" + currentPrice +
                ", marketValue=" + marketValue +
                ", weightInPortfolio=" + weightInPortfolio +
                '}';
    }
}
