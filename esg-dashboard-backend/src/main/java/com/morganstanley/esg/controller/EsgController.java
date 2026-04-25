package com.morganstanley.esg.controller;

import com.morganstanley.esg.dto.EsgScoreDTO;
import com.morganstanley.esg.dto.EsgMetricDTO;
import com.morganstanley.esg.service.PortfolioService;
import com.morganstanley.esg.repository.EsgScoreRepository;
import com.morganstanley.esg.repository.EsgMetricRepository;
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

/**
 * REST Controller for ESG operations.
 * 
 * Provides endpoints for ESG score management, portfolio-level ESG metrics,
 * and ESG analytics.
 */
@RestController
@RequestMapping("/esg")
@Tag(name = "ESG Management", description = "APIs for managing Environmental, Social, and Governance scores")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class EsgController {

    private static final Logger logger = LoggerFactory.getLogger(EsgController.class);

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private EsgScoreRepository esgScoreRepository;

    @Autowired
    private EsgMetricRepository esgMetricRepository;

    /**
     * Update ESG score for a holding.
     */
    @Operation(summary = "Update ESG score", description = "Updates or creates an ESG score for a specific holding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "ESG score created/updated successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Holding not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/holdings/{holdingId}/esg-score")
    public ResponseEntity<EsgScoreDTO> updateEsgScore(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId,
            @Valid @RequestBody EsgScoreDTO esgScoreDTO) {
        logger.info("POST /esg/holdings/{}/esg-score - Updating ESG score", holdingId);
        
        esgScoreDTO.setHoldingId(holdingId);
        EsgScoreDTO updatedScore = portfolioService.updateEsgScore(holdingId, esgScoreDTO);
        logger.info("ESG score updated successfully for holding ID: {}", holdingId);
        
        return new ResponseEntity<>(updatedScore, HttpStatus.CREATED);
    }

    /**
     * Get ESG scores for a holding.
     */
    @Operation(summary = "Get ESG scores for holding", description = "Retrieves all ESG scores for a specific holding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ESG scores retrieved successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class))),
        @ApiResponse(responseCode = "404", description = "Holding not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/holdings/{holdingId}/scores")
    public ResponseEntity<List<EsgScoreDTO>> getEsgScoresForHolding(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId) {
        logger.debug("GET /esg/holdings/{}/scores - Fetching ESG scores", holdingId);
        
        List<EsgScore> scores = esgScoreRepository.findByHoldingId(holdingId);
        List<EsgScoreDTO> scoreDTOs = scores.stream()
                .map(this::convertToDTO)
                .toList();
        
        logger.debug("Retrieved {} ESG scores for holding ID: {}", scoreDTOs.size(), holdingId);
        return ResponseEntity.ok(scoreDTOs);
    }

    /**
     * Get latest ESG score for a holding.
     */
    @Operation(summary = "Get latest ESG score", description = "Retrieves the most recent ESG score for a specific holding")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Latest ESG score retrieved successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class))),
        @ApiResponse(responseCode = "404", description = "Holding not found or no scores available"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/holdings/{holdingId}/latest-score")
    public ResponseEntity<EsgScoreDTO> getLatestEsgScoreForHolding(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId) {
        logger.debug("GET /esg/holdings/{}/latest-score - Fetching latest ESG score", holdingId);
        
        return esgScoreRepository.findLatestByHoldingId(holdingId)
                .map(score -> {
                    EsgScoreDTO dto = convertToDTO(score);
                    logger.debug("Latest ESG score retrieved for holding ID: {}", holdingId);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get ESG scores for a portfolio.
     */
    @Operation(summary = "Get ESG scores for portfolio", description = "Retrieves all latest ESG scores for holdings in a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ESG scores retrieved successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class))),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/scores")
    public ResponseEntity<List<EsgScoreDTO>> getEsgScoresForPortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /esg/portfolios/{}/scores - Fetching ESG scores", portfolioId);
        
        List<EsgScore> scores = esgScoreRepository.findLatestByPortfolioId(portfolioId);
        List<EsgScoreDTO> scoreDTOs = scores.stream()
                .map(this::convertToDTO)
                .toList();
        
        logger.debug("Retrieved {} ESG scores for portfolio ID: {}", scoreDTOs.size(), portfolioId);
        return ResponseEntity.ok(scoreDTOs);
    }

    /**
     * Get ESG trend for a holding.
     */
    @Operation(summary = "Get ESG trend", description = "Retrieves ESG score trend for a holding over a specified date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ESG trend retrieved successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "404", description = "Holding not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/holdings/{holdingId}/trend")
    public ResponseEntity<List<EsgScoreDTO>> getEsgTrendForHolding(
            @Parameter(description = "Holding ID", required = true)
            @PathVariable Long holdingId,
            @Parameter(description = "Start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("GET /esg/holdings/{}/trend - Fetching ESG trend", holdingId);
        
        LocalDate defaultStart = LocalDate.now().minusMonths(12);
        LocalDate defaultEnd = LocalDate.now();
        
        LocalDate actualStart = startDate != null ? startDate : defaultStart;
        LocalDate actualEnd = endDate != null ? endDate : defaultEnd;
        
        if (actualStart.isAfter(actualEnd)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<EsgScore> trend = esgScoreRepository.findEsgTrendForHolding(holdingId, actualStart, actualEnd);
        List<EsgScoreDTO> trendDTOs = trend.stream()
                .map(this::convertToDTO)
                .toList();
        
        logger.debug("Retrieved ESG trend with {} points for holding ID: {}", trendDTOs.size(), holdingId);
        return ResponseEntity.ok(trendDTOs);
    }

    /**
     * Get ESG rating distribution for a portfolio.
     */
    @Operation(summary = "Get ESG rating distribution", description = "Retrieves the distribution of ESG ratings for holdings in a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rating distribution retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/rating-distribution")
    public ResponseEntity<Object> getEsgRatingDistribution(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /esg/portfolios/{}/rating-distribution - Fetching rating distribution", portfolioId);
        
        List<Object[]> distribution = esgScoreRepository.getEsgRatingDistribution(portfolioId);
        logger.debug("Rating distribution retrieved for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get ESG pillar averages for a portfolio.
     */
    @Operation(summary = "Get ESG pillar averages", description = "Retrieves average scores for Environmental, Social, and Governance pillars")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pillar averages retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios/{portfolioId}/pillar-averages")
    public ResponseEntity<Object> getEsgPillarAverages(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {
        logger.debug("GET /esg/portfolios/{}/pillar-averages - Fetching pillar averages", portfolioId);
        
        Object[] averages = esgScoreRepository.getEsgPillarAverages(portfolioId);
        logger.debug("Pillar averages retrieved for portfolio ID: {}", portfolioId);
        
        return ResponseEntity.ok(averages);
    }

    /**
     * Search ESG scores with filters.
     */
    @Operation(summary = "Search ESG scores", description = "Searches ESG scores with optional filters for date range, score range, and controversy level")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class)))
    })
    @GetMapping("/scores/search")
    public ResponseEntity<List<EsgScoreDTO>> searchEsgScores(
            @Parameter(description = "Holding ID")
            @RequestParam(required = false) Long holdingId,
            @Parameter(description = "Start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Minimum overall score")
            @RequestParam(required = false) BigDecimal minOverallScore,
            @Parameter(description = "Maximum overall score")
            @RequestParam(required = false) BigDecimal maxOverallScore,
            @Parameter(description = "Controversy level")
            @RequestParam(required = false) String controversyLevel,
            @Parameter(description = "Data provider")
            @RequestParam(required = false) String dataProvider) {
        logger.debug("GET /esg/scores/search - Searching ESG scores with filters");
        
        // Convert controversy level string to enum if provided
        EsgScore.ControversyLevel controversyEnum = null;
        if (controversyLevel != null) {
            try {
                controversyEnum = EsgScore.ControversyLevel.valueOf(controversyLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<EsgScore> scores = esgScoreRepository.searchEsgScores(
                holdingId, startDate, endDate, minOverallScore, maxOverallScore,
                controversyEnum, dataProvider, null).getContent();
        
        List<EsgScoreDTO> scoreDTOs = scores.stream()
                .map(this::convertToDTO)
                .toList();
        
        logger.debug("Search returned {} ESG scores", scoreDTOs.size());
        return ResponseEntity.ok(scoreDTOs);
    }

    /**
     * Get holdings with high ESG scores.
     */
    @Operation(summary = "Get high ESG score holdings", description = "Retrieves holdings with ESG scores above the specified threshold")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "High ESG score holdings retrieved successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class)))
    })
    @GetMapping("/holdings/high-scores")
    public ResponseEntity<List<EsgScoreDTO>> getHoldingsWithHighEsgScores(
            @Parameter(description = "Minimum score threshold")
            @RequestParam(defaultValue = "70") BigDecimal minScore) {
        logger.debug("GET /esg/holdings/high-scores - Fetching holdings with ESG scores >= {}", minScore);
        
        List<EsgScore> scores = esgScoreRepository.findHoldingsWithHighEsgScores(minScore);
        List<EsgScoreDTO> scoreDTOs = scores.stream()
                .map(this::convertToDTO)
                .toList();
        
        logger.debug("Retrieved {} holdings with high ESG scores", scoreDTOs.size());
        return ResponseEntity.ok(scoreDTOs);
    }

    /**
     * Get holdings with controversy issues.
     */
    @Operation(summary = "Get holdings with controversies", description = "Retrieves holdings with controversy issues")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Holdings with controversies retrieved successfully",
                content = @Content(schema = @Schema(implementation = EsgScoreDTO.class)))
    })
    @GetMapping("/holdings/controversies")
    public ResponseEntity<List<EsgScoreDTO>> getHoldingsWithControversies() {
        logger.debug("GET /esg/holdings/controversies - Fetching holdings with controversy issues");
        
        List<EsgScore.ControversyLevel> controversyLevels = List.of(
                EsgScore.ControversyLevel.MODERATE,
                EsgScore.ControversyLevel.HIGH,
                EsgScore.ControversyLevel.VERY_HIGH
        );
        
        List<EsgScore> scores = esgScoreRepository.findHoldingsWithControversies(controversyLevels);
        List<EsgScoreDTO> scoreDTOs = scores.stream()
                .map(this::convertToDTO)
                .toList();
        
        logger.debug("Retrieved {} holdings with controversy issues", scoreDTOs.size());
        return ResponseEntity.ok(scoreDTOs);
    }

    /**
     * Health check endpoint.
     */
    @Operation(summary = "Health check", description = "Verifies that the ESG service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("GET /esg/health - Health check");
        return ResponseEntity.ok("ESG service is healthy");
    }

    // Helper method for conversion
    private EsgScoreDTO convertToDTO(EsgScore esgScore) {
        EsgScoreDTO dto = new EsgScoreDTO();
        dto.setId(esgScore.getId());
        dto.setHoldingId(esgScore.getHolding() != null ? esgScore.getHolding().getId() : null);
        dto.setScoreDate(esgScore.getScoreDate());
        dto.setOverallScore(esgScore.getOverallScore());
        dto.setEnvironmentalPillar(esgScore.getEnvironmentalPillar());
        dto.setSocialPillar(esgScore.getSocialPillar());
        dto.setGovernancePillar(esgScore.getGovernancePillar());
        dto.setControversyLevel(esgScore.getControversyLevel().toString());
        dto.setDataProvider(esgScore.getDataProvider());
        dto.setCreatedAt(esgScore.getCreatedAt());
        
        // Calculate derived fields
        dto.setEsgRating(esgScore.getEsgRating());
        dto.setPillarAverage(esgScore.getPillarAverage());
        
        return dto;
    }
}
