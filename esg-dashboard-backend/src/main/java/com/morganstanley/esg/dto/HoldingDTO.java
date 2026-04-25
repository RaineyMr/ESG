package com.morganstanley.esg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Holding entity.
 * 
 * Used for API request/response to transfer holding data between
 * client and server while maintaining proper validation and serialization.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoldingDTO {

    private Long id;
    private Long portfolioId;

    @NotBlank(message = "Ticker symbol is required")
    @Size(max = 10, message = "Ticker symbol must not exceed 10 characters")
    private String tickerSymbol;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @NotBlank(message = "Sector is required")
    @Size(max = 100, message = "Sector must not exceed 100 characters")
    private String sector;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0001", message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0001", message = "Current price must be positive")
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.0", message = "Market value must be non-negative")
    private BigDecimal marketValue;

    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    private BigDecimal weightInPortfolio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<EsgScoreDTO> esgScores;
    private List<RiskMetricDTO> riskMetrics;

    // Calculated fields (not persisted)
    private BigDecimal unrealizedGainLoss;
    private BigDecimal unrealizedGainLossPercentage;

    // Default constructor
    public HoldingDTO() {
    }

    // Constructor with required fields
    public HoldingDTO(String tickerSymbol, String companyName, String sector, 
                     BigDecimal quantity, BigDecimal purchasePrice, BigDecimal currentPrice) {
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

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
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
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
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

    public List<EsgScoreDTO> getEsgScores() {
        return esgScores;
    }

    public void setEsgScores(List<EsgScoreDTO> esgScores) {
        this.esgScores = esgScores;
    }

    public List<RiskMetricDTO> getRiskMetrics() {
        return riskMetrics;
    }

    public void setRiskMetrics(List<RiskMetricDTO> riskMetrics) {
        this.riskMetrics = riskMetrics;
    }

    public BigDecimal getUnrealizedGainLoss() {
        return unrealizedGainLoss;
    }

    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) {
        this.unrealizedGainLoss = unrealizedGainLoss;
    }

    public BigDecimal getUnrealizedGainLossPercentage() {
        return unrealizedGainLossPercentage;
    }

    public void setUnrealizedGainLossPercentage(BigDecimal unrealizedGainLossPercentage) {
        this.unrealizedGainLossPercentage = unrealizedGainLossPercentage;
    }

    @Override
    public String toString() {
        return "HoldingDTO{" +
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
