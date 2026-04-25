package com.morganstanley.esg.controller;

import com.morganstanley.esg.dto.RiskMetricDTO;
import com.morganstanley.esg.service.PortfolioService;
import com.morganstanley.esg.repository.RiskMetricRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Risk Management operations.
 * 
 * Provides endpoints for risk metric management, portfolio risk analysis,
 * and risk assessment tools.
 */
@RestController
@RequestMapping("/risk")
@Tag(name = "Risk Management", description = "APIs for managing investment risk metrics and analysis")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class RiskController {

    private static final Logger logger = LoggerFactory.getLogger(RiskController.class);

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private RiskMetricRepository riskMetricRepository;

    /**
     * Add risk metric for a holding.
     */
    @Operation(summary = "Add risk metric", description = "Adds or updates risk metrics for a specific holding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Risk metric added successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Holding not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/holdings/{holdingId}/risk-metric")
    public ResponseEntity<RiskMetricDTO> addRiskMetric(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId,
            @Valid @RequestBody RiskMetricDTO riskMetricDTO) {
        logger.info("POST /risk/holdings/{}/risk-metric - Adding risk metric", holdingId);
        
        riskMetricDTO.setHoldingId(holdingId);
        RiskMetricDTO createdMetric = portfolioService.addRiskMetric(holdingId, riskMetricDTO);
        logger.info("Risk metric added successfully for holding ID: {}", holdingId);
        
        return new ResponseEntity<>(createdMetric, HttpStatus.CREATED);
    }

    /**
     * Get risk metrics for a holding.
     */
    @Operation(summary = "Get risk metrics for holding", description = "Retrieves all risk metrics for a specific holding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Risk metrics retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class))),
        @ApiResponse(responseCode = "404", description = "Holding not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/holdings/{holdingId}/metrics")
    public ResponseEntity<List<RiskMetricDTO>> getRiskMetricsForHolding(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId) {
        logger.debug("GET /risk/holdings/{}/metrics - Fetching risk metrics", holdingId);
        
        List<RiskMetric> metrics = riskMetricRepository.findByHoldingId(holdingId);
        List<RiskMetricDTO> metricDTOs = metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved {} risk metrics for holding ID: {}", metricDTOs.size(), holdingId);
        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get latest risk metric for a holding.
     */
    @Operation(summary = "Get latest risk metric", description = "Retrieves the most recent risk metric for a specific holding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Latest risk metric retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class))),
        @ApiResponse(responseCode = "404", description = "Holding not found or no metrics available"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/holdings/{holdingId}/latest-metric")
    public ResponseEntity<RiskMetricDTO> getLatestRiskMetricForHolding(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId) {
        logger.debug("GET /risk/holdings/{}/latest-metric - Fetching latest risk metric", holdingId);
        
        return riskMetricRepository.findLatestByHoldingId(holdingId)
                .map(metric -> {
                    RiskMetricDTO dto = convertToDTO(metric);
                    logger.debug("Latest risk metric retrieved for holding ID: {}", holdingId);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get risk metrics for a portfolio.
     */
    @Operation(summary = "Get risk metrics for portfolio", description = "Retrieves all latest risk metrics for holdings in a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Risk metrics retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class))),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/metrics")
    public ResponseEntity<List<RiskMetricDTO>> getRiskMetricsForPortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/metrics - Fetching risk metrics", portfolioId);
        
        List<RiskMetric> metrics = riskMetricRepository.findLatestByPortfolioId(portfolioId);
        List<RiskMetricDTO> metricDTOs = metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved {} risk metrics for portfolio ID: {}", metricDTOs.size(), portfolioId);
        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get risk trend for a holding.
     */
    @Operation(summary = "Get risk trend", description = "Retrieves risk metric trend for a holding over a specified date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Risk trend retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "404", description = "Holding not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/holdings/{holdingId}/trend")
    public ResponseEntity<List<RiskMetricDTO>> getRiskTrendForHolding(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId,
            @Parameter(description = "Start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("GET /risk/holdings/{}/trend - Fetching risk trend", holdingId);
        
        LocalDate defaultStart = LocalDate.now().minusMonths(12);
        LocalDate defaultEnd = LocalDate.now();
        
        LocalDate actualStart = startDate != null ? startDate : defaultStart;
        LocalDate actualEnd = endDate != null ? endDate : defaultEnd;
        
        if (actualStart.isAfter(actualEnd)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<RiskMetric> trend = riskMetricRepository.findRiskTrendForHolding(holdingId, actualStart, actualEnd);
        List<RiskMetricDTO> trendDTOs = trend.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved risk trend with {} points for holding ID: {}", trendDTOs.size(), holdingId);
        return ResponseEntity.ok(trendDTOs);
    }

    /**
     * Get risk rating distribution for a portfolio.
     */
    @Operation(summary = "Get risk rating distribution", description = "Retrieves the distribution of risk ratings for holdings in a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rating distribution retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/rating-distribution")
    public ResponseEntity<Object> getRiskRatingDistribution(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/rating-distribution - Fetching rating distribution", portfolioId);
        
        List<Object[]> distribution = riskMetricRepository.getRiskRatingDistribution(portfolioId);
        logger.debug("Risk rating distribution retrieved for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get risk averages for a portfolio.
     */
    @Operation(summary = "Get risk averages", description = "Retrieves average risk metrics for holdings in a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Risk averages retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/averages")
    public ResponseEntity<Object> getRiskAverages(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/averages - Fetching risk averages", portfolioId);
        
        Object[] averages = riskMetricRepository.getRiskAverages(portfolioId);
        logger.debug("Risk averages retrieved for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(averages);
    }

    /**
     * Get high-risk holdings.
     */
    @Operation(summary = "Get high-risk holdings", description = "Retrieves holdings with specified high risk ratings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "High-risk holdings retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class)))
    })
    @GetMapping("/holdings/high-risk")
    public ResponseEntity<List<RiskMetricDTO>> getHighRiskHoldings(
            @Parameter(description = "Risk rating levels to include")
            @RequestParam(defaultValue = "HIGH,VERY_HIGH") String riskRatings) {
        logger.debug("GET /risk/holdings/high-risk - Fetching high-risk holdings");
        
        List<String> ratingStrings = List.of(riskRatings.split(","));
        List<RiskMetric.RiskRating> ratings = ratingStrings.stream()
                .map(String::trim)
                .map(RiskMetric.RiskRating::valueOf)
                .collect(Collectors.toList());
        
        List<RiskMetric> metrics = riskMetricRepository.findHighRiskHoldings(ratings);
        List<RiskMetricDTO> metricDTOs = metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved {} high-risk holdings", metricDTOs.size());
        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get high-beta holdings.
     */
    @Operation(summary = "Get high-beta holdings", description = "Retrieves holdings with beta values above the specified threshold")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "High-beta holdings retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class)))
    })
    @GetMapping("/holdings/high-beta")
    public ResponseEntity<List<RiskMetricDTO>> getHighBetaHoldings(
            @Parameter(description = "Minimum beta threshold")
            @RequestParam(defaultValue = "1.5") BigDecimal minBeta) {
        logger.debug("GET /risk/holdings/high-beta - Fetching holdings with beta >= {}", minBeta);
        
        List<RiskMetric> metrics = riskMetricRepository.findHighBetaHoldings(minBeta);
        List<RiskMetricDTO> metricDTOs = metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved {} high-beta holdings", metricDTOs.size());
        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get high-volatility holdings.
     */
    @Operation(summary = "Get high-volatility holdings", description = "Retrieves holdings with volatility above the specified threshold")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "High-volatility holdings retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class)))
    })
    @GetMapping("/holdings/high-volatility")
    public ResponseEntity<List<RiskMetricDTO>> getHighVolatilityHoldings(
            @Parameter(description = "Minimum volatility threshold")
            @RequestParam(defaultValue = "0.30") BigDecimal minVolatility) {
        logger.debug("GET /risk/holdings/high-volatility - Fetching holdings with volatility >= {}", minVolatility);
        
        List<RiskMetric> metrics = riskMetricRepository.findHighVolatilityHoldings(minVolatility);
        List<RiskMetricDTO> metricDTOs = metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved {} high-volatility holdings", metricDTOs.size());
        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get holdings with negative Sharpe ratio.
     */
    @Operation(summary = "Get negative Sharpe ratio holdings", description = "Retrieves holdings with negative Sharpe ratios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Negative Sharpe ratio holdings retrieved successfully",
                content = @Content(schema = @Schema(implementation = RiskMetricDTO.class)))
    })
    @GetMapping("/holdings/negative-sharpe")
    public ResponseEntity<List<RiskMetricDTO>> getNegativeSharpeHoldings() {
        logger.debug("GET /risk/holdings/negative-sharpe - Fetching holdings with negative Sharpe ratio");
        
        List<RiskMetric> metrics = riskMetricRepository.findNegativeSharpeHoldings();
        List<RiskMetricDTO> metricDTOs = metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Retrieved {} holdings with negative Sharpe ratio", metricDTOs.size());
        return ResponseEntity.ok(metricDTOs);
    }

    /**
     * Get risk metrics by sector for a portfolio.
     */
    @Operation(summary = "Get risk metrics by sector", description = "Retrieves risk metrics grouped by sector for a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Risk metrics by sector retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/by-sector")
    public ResponseEntity<Object> getRiskMetricsBySector(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/by-sector - Fetching risk metrics by sector", portfolioId);
        
        List<Object[]> sectorMetrics = riskMetricRepository.getRiskMetricsBySector(portfolioId);
        logger.debug("Risk metrics by sector retrieved for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(sectorMetrics);
    }

    /**
     * Calculate portfolio risk score.
     */
    @Operation(summary = "Calculate portfolio risk score", description = "Calculates a composite risk score for a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio risk score calculated successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/risk-score")
    public ResponseEntity<Object> calculatePortfolioRiskScore(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/risk-score - Calculating portfolio risk score", portfolioId);
        
        BigDecimal riskScore = riskMetricRepository.calculatePortfolioRiskScore(portfolioId);
        logger.debug("Portfolio risk score calculated for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(riskScore);
    }

    /**
     * Calculate portfolio beta.
     */
    @Operation(summary = "Calculate portfolio beta", description = "Calculates the weighted average beta for a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio beta calculated successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/beta")
    public ResponseEntity<Object> calculatePortfolioBeta(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/beta - Calculating portfolio beta", portfolioId);
        
        BigDecimal beta = riskMetricRepository.calculatePortfolioBeta(portfolioId);
        logger.debug("Portfolio beta calculated for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(beta);
    }

    /**
     * Calculate portfolio Value at Risk.
     */
    @Operation(summary = "Calculate portfolio VaR", description = "Calculates the weighted average Value at Risk for a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio VaR calculated successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/var")
    public ResponseEntity<Object> calculatePortfolioValueAtRisk(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /risk/portfolios/{}/var - Calculating portfolio VaR", portfolioId);
        
        BigDecimal var = riskMetricRepository.calculatePortfolioValueAtRisk(portfolioId);
        logger.debug("Portfolio VaR calculated for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(var);
    }

    /**
     * Health check endpoint.
     */
    @Operation(summary = "Health check", description = "Verifies that the risk service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("GET /risk/health - Health check");
        return ResponseEntity.ok("Risk service is healthy");
    }

    // Helper method for conversion
    private RiskMetricDTO convertToDTO(RiskMetric riskMetric) {
        RiskMetricDTO dto = new RiskMetricDTO();
        dto.setId(riskMetric.getId());
        dto.setHoldingId(riskMetric.getHolding() != null ? riskMetric.getHolding().getId() : null);
        dto.setCalculationDate(riskMetric.getCalculationDate());
        dto.setBeta(riskMetric.getBeta());
        dto.setVolatility(riskMetric.getVolatility());
        dto.setValueAtRisk(riskMetric.getValueAtRisk());
        dto.setMaxDrawdown(riskMetric.getMaxDrawdown());
        dto.setSharpeRatio(riskMetric.getSharpeRatio());
        dto.setRiskRating(riskMetric.getRiskRating().toString());
        dto.setTimeHorizonDays(riskMetric.getTimeHorizonDays());
        dto.setCreatedAt(riskMetric.getCreatedAt());
        
        // Calculate derived fields
        dto.setRiskLevelDescription(riskMetric.getRiskLevelDescription());
        dto.setAnnualizedVolatility(riskMetric.getAnnualizedVolatility());
        dto.setHighBeta(riskMetric.isHighBeta());
        dto.setHighVolatility(riskMetric.isHighVolatility());
        dto.setNegativeSharpeRatio(riskMetric.hasNegativeSharpeRatio());
        
        return dto;
    }
}
