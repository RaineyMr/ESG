package com.morganstanley.esg.controller;

import com.morganstanley.esg.dto.PortfolioDTO;
import com.morganstanley.esg.dto.HoldingDTO;
import com.morganstanley.esg.dto.PortfolioSummaryDTO;
import com.morganstanley.esg.service.PortfolioService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Portfolio operations.
 * 
 * Provides endpoints for portfolio management including CRUD operations,
 * analytics, ESG calculations, and risk assessments.
 */
@RestController
@RequestMapping("/portfolios")
@Tag(name = "Portfolio Management", description = "APIs for managing investment portfolios")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    @Autowired
    private PortfolioService portfolioService;

    /**
     * Create a new portfolio.
     */
    @Operation(summary = "Create a new portfolio", description = "Creates a new investment portfolio with the specified details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Portfolio created successfully",
                content = @Content(schema = @Schema(implementation = PortfolioDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<PortfolioDTO> createPortfolio(@Valid @RequestBody PortfolioDTO portfolioDTO) {
        logger.info("POST /portfolios - Creating portfolio: {}", portfolioDTO.getPortfolioName());
        
        PortfolioDTO createdPortfolio = portfolioService.createPortfolio(portfolioDTO);
        logger.info("Portfolio created successfully with ID: {}", createdPortfolio.getId());
        
        return new ResponseEntity<>(createdPortfolio, HttpStatus.CREATED);
    }

    /**
     * Get all portfolios.
     */
    @Operation(summary = "Get all portfolios", description = "Retrieves a list of all investment portfolios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolios retrieved successfully",
                content = @Content(schema = @Schema(implementation = PortfolioDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<PortfolioDTO>> getAllPortfolios() {
        logger.debug("GET /portfolios - Fetching all portfolios");
        
        List<PortfolioDTO> portfolios = portfolioService.getAllPortfolios();
        logger.debug("Retrieved {} portfolios", portfolios.size());
        
        return ResponseEntity.ok(portfolios);
    }

    /**
     * Get portfolio by ID.
     */
    @Operation(summary = "Get portfolio by ID", description = "Retrieves a specific portfolio by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio retrieved successfully",
                content = @Content(schema = @Schema(implementation = PortfolioDTO.class))),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolioById(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long id) {
        logger.debug("GET /portfolios/{} - Fetching portfolio", id);
        
        PortfolioDTO portfolio = portfolioService.getPortfolioById(id);
        logger.debug("Portfolio retrieved: {}", portfolio.getPortfolioName());
        
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Update an existing portfolio.
     */
    @Operation(summary = "Update portfolio", description = "Updates an existing portfolio with new details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio updated successfully",
                content = @Content(schema = @Schema(implementation = PortfolioDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioDTO> updatePortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PortfolioDTO portfolioDTO) {
        logger.info("PUT /portfolios/{} - Updating portfolio", id);
        
        PortfolioDTO updatedPortfolio = portfolioService.updatePortfolio(id, portfolioDTO);
        logger.info("Portfolio updated successfully: {}", updatedPortfolio.getPortfolioName());
        
        return ResponseEntity.ok(updatedPortfolio);
    }

    /**
     * Delete a portfolio.
     */
    @Operation(summary = "Delete portfolio", description = "Deletes a portfolio and all its associated holdings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Portfolio deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long id) {
        logger.info("DELETE /portfolios/{} - Deleting portfolio", id);
        
        portfolioService.deletePortfolio(id);
        logger.info("Portfolio deleted successfully");
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get portfolio summary with comprehensive metrics.
     */
    @Operation(summary = "Get portfolio summary", description = "Retrieves comprehensive portfolio analytics including ESG scores, risk metrics, and performance data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio summary retrieved successfully",
                content = @Content(schema = @Schema(implementation = PortfolioSummaryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/summary")
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioSummary(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long id) {
        logger.debug("GET /portfolios/{}/summary - Generating portfolio summary", id);
        
        PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary(id);
        logger.debug("Portfolio summary generated for: {}", summary.getPortfolioName());
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Add a holding to a portfolio.
     */
    @Operation(summary = "Add holding to portfolio", description = "Adds a new security holding to an existing portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Holding added successfully",
                content = @Content(schema = @Schema(implementation = HoldingDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/holdings")
    public ResponseEntity<HoldingDTO> addHoldingToPortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody HoldingDTO holdingDTO) {
        logger.info("POST /portfolios/{}/holdings - Adding holding: {}", id, holdingDTO.getTickerSymbol());
        
        HoldingDTO createdHolding = portfolioService.addHoldingToPortfolio(id, holdingDTO);
        logger.info("Holding added successfully with ID: {}", createdHolding.getId());
        
        return new ResponseEntity<>(createdHolding, HttpStatus.CREATED);
    }

    /**
     * Search portfolios with filters.
     */
    @Operation(summary = "Search portfolios", description = "Searches portfolios with optional filters for name, currency, and value range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                content = @Content(schema = @Schema(implementation = PortfolioDTO.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<PortfolioDTO>> searchPortfolios(
            @Parameter(description = "Portfolio name (partial match)")
            @RequestParam(required = false) String portfolioName,
            @Parameter(description = "Base currency (ISO 3-letter code)")
            @RequestParam(required = false) String baseCurrency,
            @Parameter(description = "Minimum total value")
            @RequestParam(required = false) BigDecimal minValue,
            @Parameter(description = "Maximum total value")
            @RequestParam(required = false) BigDecimal maxValue,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        logger.debug("GET /portfolios/search - Searching portfolios with filters");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioDTO> portfolios = portfolioService.searchPortfolios(
                portfolioName, baseCurrency, minValue, maxValue, pageable);
        
        logger.debug("Search returned {} portfolios", portfolios.getTotalElements());
        return ResponseEntity.ok(portfolios);
    }

    /**
     * Get portfolio statistics.
     */
    @Operation(summary = "Get portfolio statistics", description = "Retrieves aggregated statistics across all portfolios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Object> getPortfolioStatistics() {
        logger.debug("GET /portfolios/statistics - Generating portfolio statistics");
        
        Object statistics = portfolioService.getPortfolioStatistics();
        logger.debug("Portfolio statistics generated");
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Health check endpoint.
     */
    @Operation(summary = "Health check", description = "Verifies that the portfolio service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("GET /portfolios/health - Health check");
        return ResponseEntity.ok("Portfolio service is healthy");
    }
}
