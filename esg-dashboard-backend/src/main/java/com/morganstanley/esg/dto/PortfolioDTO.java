package com.morganstanley.esg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Portfolio entity.
 * 
 * Used for API request/response to transfer portfolio data between
 * client and server while maintaining proper validation and serialization.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioDTO {

    private Long id;

    @NotBlank(message = "Portfolio name is required")
    @Size(max = 255, message = "Portfolio name must not exceed 255 characters")
    private String portfolioName;

    private String description;

    @DecimalMin(value = "0.0", message = "Total value must be non-negative")
    private BigDecimal totalValue;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String baseCurrency;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime inceptionDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<HoldingDTO> holdings;
    private List<EsgMetricDTO> esgMetrics;

    // Default constructor
    public PortfolioDTO() {
    }

    // Constructor with required fields
    public PortfolioDTO(String portfolioName, BigDecimal totalValue, String baseCurrency) {
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

    public List<HoldingDTO> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<HoldingDTO> holdings) {
        this.holdings = holdings;
    }

    public List<EsgMetricDTO> getEsgMetrics() {
        return esgMetrics;
    }

    public void setEsgMetrics(List<EsgMetricDTO> esgMetrics) {
        this.esgMetrics = esgMetrics;
    }

    @Override
    public String toString() {
        return "PortfolioDTO{" +
                "id=" + id +
                ", portfolioName='" + portfolioName + '\'' +
                ", totalValue=" + totalValue +
                ", baseCurrency='" + baseCurrency + '\'' +
                ", inceptionDate=" + inceptionDate +
                '}';
    }
}
