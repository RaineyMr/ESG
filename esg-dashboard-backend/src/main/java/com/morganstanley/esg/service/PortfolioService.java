package com.morganstanley.esg.service;

import com.morganstanley.esg.dto.*;
import com.morganstanley.esg.model.*;
import com.morganstanley.esg.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Portfolio business logic.
 * 
 * Provides comprehensive portfolio management operations including
 * CRUD operations, analytics, ESG calculations, and risk assessments.
 */
@Service
@Transactional
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private EsgScoreRepository esgScoreRepository;

    @Autowired
    private EsgMetricRepository esgMetricRepository;

    @Autowired
    private RiskMetricRepository riskMetricRepository;

    /**
     * Create a new portfolio.
     */
    @CacheEvict(value = "portfolios", allEntries = true)
    public PortfolioDTO createPortfolio(PortfolioDTO portfolioDTO) {
        logger.info("Creating new portfolio: {}", portfolioDTO.getPortfolioName());

        Portfolio portfolio = convertToEntity(portfolioDTO);
        portfolio.setCreatedAt(LocalDateTime.now());
        portfolio.setUpdatedAt(LocalDateTime.now());

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        logger.info("Portfolio created with ID: {}", savedPortfolio.getId());

        return convertToDTO(savedPortfolio);
    }

    /**
     * Get all portfolios.
     */
    @Cacheable(value = "portfolios")
    @Transactional(readOnly = true)
    public List<PortfolioDTO> getAllPortfolios() {
        logger.debug("Fetching all portfolios");
        return portfolioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get portfolio by ID.
     */
    @Transactional(readOnly = true)
    public PortfolioDTO getPortfolioById(Long id) {
        logger.debug("Fetching portfolio with ID: {}", id);
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + id));
        return convertToDTO(portfolio);
    }

    /**
     * Update an existing portfolio.
     */
    @CacheEvict(value = "portfolios", allEntries = true)
    public PortfolioDTO updatePortfolio(Long id, PortfolioDTO portfolioDTO) {
        logger.info("Updating portfolio with ID: {}", id);

        Portfolio existingPortfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + id));

        updatePortfolioFields(existingPortfolio, portfolioDTO);
        existingPortfolio.setUpdatedAt(LocalDateTime.now());

        Portfolio updatedPortfolio = portfolioRepository.save(existingPortfolio);
        logger.info("Portfolio updated successfully");

        return convertToDTO(updatedPortfolio);
    }

    /**
     * Delete a portfolio.
     */
    @CacheEvict(value = "portfolios", allEntries = true)
    public void deletePortfolio(Long id) {
        logger.info("Deleting portfolio with ID: {}", id);
        
        if (!portfolioRepository.existsById(id)) {
            throw new RuntimeException("Portfolio not found with ID: " + id);
        }
        
        portfolioRepository.deleteById(id);
        logger.info("Portfolio deleted successfully");
    }

    /**
     * Get portfolio summary with comprehensive metrics.
     */
    @Transactional(readOnly = true)
    public PortfolioSummaryDTO getPortfolioSummary(Long id) {
        logger.debug("Generating portfolio summary for ID: {}", id);

        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + id));

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO(
                portfolio.getId(),
                portfolio.getPortfolioName(),
                portfolio.getTotalValue(),
                portfolio.getBaseCurrency(),
                portfolio.getInceptionDate()
        );

        // Basic metrics
        summary.setHoldingCount(portfolio.getHoldings().size());
        summary.setLastUpdated(portfolio.getUpdatedAt());

        // Calculate ESG metrics
        calculatePortfolioEsgMetrics(summary, portfolio);

        // Calculate risk metrics
        calculatePortfolioRiskMetrics(summary, portfolio);

        // Calculate performance metrics
        calculatePortfolioPerformanceMetrics(summary, portfolio);

        // Calculate portfolio concentration
        calculatePortfolioConcentration(summary, portfolio);

        // Calculate ratings
        summary.calculateEsgRating();
        summary.calculateOverallRiskLevel();

        logger.debug("Portfolio summary generated for ID: {}", id);
        return summary;
    }

    /**
     * Add a holding to a portfolio.
     */
    @CacheEvict(value = "portfolios", allEntries = true)
    public HoldingDTO addHoldingToPortfolio(Long portfolioId, HoldingDTO holdingDTO) {
        logger.info("Adding holding {} to portfolio {}", holdingDTO.getTickerSymbol(), portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));

        Holding holding = convertToEntity(holdingDTO);
        holding.setPortfolio(portfolio);

        // Calculate market value and weight
        holding.setMarketValue(holding.getCurrentPrice().multiply(holding.getQuantity()));
        updateHoldingWeight(holding, portfolio);

        Holding savedHolding = holdingRepository.save(holding);

        // Update portfolio total value
        portfolio.updateTotalValue();
        portfolioRepository.save(portfolio);

        logger.info("Holding added successfully with ID: {}", savedHolding.getId());
        return convertToDTO(savedHolding);
    }

    /**
     * Update ESG score for a holding.
     */
    @CacheEvict(value = {"portfolios", "esg-scores"}, allEntries = true)
    public EsgScoreDTO updateEsgScore(Long holdingId, EsgScoreDTO esgScoreDTO) {
        logger.info("Updating ESG score for holding ID: {}", holdingId);

        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + holdingId));

        EsgScore esgScore = convertToEntity(esgScoreDTO);
        esgScore.setHolding(holding);

        // Calculate pillar average and rating
        BigDecimal pillarAverage = esgScore.getEnvironmentalPillar()
                .add(esgScore.getSocialPillar())
                .add(esgScore.getGovernancePillar())
                .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        esgScore.setPillarAverage(pillarAverage);

        EsgScore savedScore = esgScoreRepository.save(esgScore);
        logger.info("ESG score updated successfully");

        return convertToDTO(savedScore);
    }

    /**
     * Add risk metric for a holding.
     */
    @CacheEvict(value = {"portfolios", "risk-metrics"}, allEntries = true)
    public RiskMetricDTO addRiskMetric(Long holdingId, RiskMetricDTO riskMetricDTO) {
        logger.info("Adding risk metric for holding ID: {}", holdingId);

        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + holdingId));

        RiskMetric riskMetric = convertToEntity(riskMetricDTO);
        riskMetric.setHolding(holding);

        // Calculate derived fields
        riskMetric.calculateAllFields();

        RiskMetric savedMetric = riskMetricRepository.save(riskMetric);
        logger.info("Risk metric added successfully");

        return convertToDTO(savedMetric);
    }

    /**
     * Search portfolios with filters.
     */
    @Transactional(readOnly = true)
    public Page<PortfolioDTO> searchPortfolios(String portfolioName, String baseCurrency,
                                              BigDecimal minValue, BigDecimal maxValue,
                                              Pageable pageable) {
        logger.debug("Searching portfolios with filters");

        Page<Portfolio> portfolios = portfolioRepository.searchPortfolios(
                portfolioName, baseCurrency, minValue, maxValue, pageable);

        return portfolios.map(this::convertToDTO);
    }

    /**
     * Get portfolio statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPortfolioStatistics() {
        logger.debug("Generating portfolio statistics");

        Object[] stats = portfolioRepository.getPortfolioStatistics();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPortfolios", stats[0]);
        statistics.put("totalValue", stats[1]);
        statistics.put("averageValue", stats[2]);
        statistics.put("minValue", stats[3]);
        statistics.put("maxValue", stats[4]);

        return statistics;
    }

    // Private helper methods

    private void updatePortfolioFields(Portfolio portfolio, PortfolioDTO dto) {
        if (dto.getPortfolioName() != null) {
            portfolio.setPortfolioName(dto.getPortfolioName());
        }
        if (dto.getDescription() != null) {
            portfolio.setDescription(dto.getDescription());
        }
        if (dto.getTotalValue() != null) {
            portfolio.setTotalValue(dto.getTotalValue());
        }
        if (dto.getBaseCurrency() != null) {
            portfolio.setBaseCurrency(dto.getBaseCurrency());
        }
        if (dto.getInceptionDate() != null) {
            portfolio.setInceptionDate(dto.getInceptionDate());
        }
    }

    private void updateHoldingWeight(Holding holding, Portfolio portfolio) {
        if (portfolio.getTotalValue() != null && portfolio.getTotalValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weight = holding.getMarketValue()
                    .divide(portfolio.getTotalValue(), 4, RoundingMode.HALF_UP);
            holding.setWeightInPortfolio(weight);
        }
    }

    private void calculatePortfolioEsgMetrics(PortfolioSummaryDTO summary, Portfolio portfolio) {
        List<EsgScore> latestScores = esgScoreRepository.findLatestByPortfolioId(portfolio.getId());
        
        if (!latestScores.isEmpty()) {
            BigDecimal avgEsgScore = latestScores.stream()
                    .map(EsgScore::getOverallScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(latestScores.size()), 2, RoundingMode.HALF_UP);
            
            summary.setAverageEsgScore(avgEsgScore);

            // Calculate pillar averages
            BigDecimal avgEnvironmental = latestScores.stream()
                    .map(EsgScore::getEnvironmentalPillar)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(latestScores.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal avgSocial = latestScores.stream()
                    .map(EsgScore::getSocialPillar)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(latestScores.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal avgGovernance = latestScores.stream()
                    .map(EsgScore::getGovernancePillar)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(latestScores.size()), 2, RoundingMode.HALF_UP);

            summary.setEnvironmentalScore(avgEnvironmental);
            summary.setSocialScore(avgSocial);
            summary.setGovernanceScore(avgGovernance);

            // Count controversies
            int controversyCount = latestScores.stream()
                    .mapToInt(score -> score.getControversyLevel() != null && 
                            !score.getControversyLevel().equals("LOW") ? 1 : 0)
                    .sum();
            summary.setControversyCount(controversyCount);
        }
    }

    private void calculatePortfolioRiskMetrics(PortfolioSummaryDTO summary, Portfolio portfolio) {
        List<RiskMetric> latestMetrics = riskMetricRepository.findLatestByPortfolioId(portfolio.getId());
        
        if (!latestMetrics.isEmpty()) {
            BigDecimal avgBeta = latestMetrics.stream()
                    .filter(metric -> metric.getBeta() != null)
                    .map(RiskMetric::getBeta)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(latestMetrics.size()), 4, RoundingMode.HALF_UP);
            
            BigDecimal avgVolatility = latestMetrics.stream()
                    .filter(metric -> metric.getVolatility() != null)
                    .map(RiskMetric::getVolatility)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(latestMetrics.size()), 4, RoundingMode.HALF_UP);

            summary.setAverageBeta(avgBeta);
            summary.setAverageVolatility(avgVolatility);

            // Determine highest risk level
            String highestRisk = latestMetrics.stream()
                    .map(metric -> metric.getRiskRating().toString())
                    .max(Comparator.comparing(risk -> Arrays.asList("LOW", "MODERATE", "HIGH", "VERY_HIGH").indexOf(risk)))
                    .orElse("UNKNOWN");
            summary.setOverallRiskLevel(highestRisk);

            // Calculate portfolio-level risk metrics
            BigDecimal portfolioBeta = riskMetricRepository.calculatePortfolioBeta(portfolio.getId());
            BigDecimal portfolioVaR = riskMetricRepository.calculatePortfolioValueAtRisk(portfolio.getId());
            
            summary.setPortfolioBeta(portfolioBeta);
            summary.setPortfolioValueAtRisk(portfolioVaR);
        }
    }

    private void calculatePortfolioPerformanceMetrics(PortfolioSummaryDTO summary, Portfolio portfolio) {
        BigDecimal totalGainLoss = portfolio.getHoldings().stream()
                .map(holding -> holding.getUnrealizedGainLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.setTotalUnrealizedGainLoss(totalGainLoss);

        if (portfolio.getTotalValue() != null && portfolio.getTotalValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal gainLossPercentage = totalGainLoss
                    .divide(portfolio.getTotalValue(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            summary.setTotalUnrealizedGainLossPercentage(gainLossPercentage);
        }
    }

    private void calculatePortfolioConcentration(PortfolioSummaryDTO summary, Portfolio portfolio) {
        BigDecimal concentration = riskMetricRepository.getPortfolioConcentration(portfolio.getId());
        if (concentration != null) {
            summary.setPortfolioConcentration(concentration.multiply(new BigDecimal("100")));
        }
    }

    // Entity-DTO conversion methods

    private Portfolio convertToEntity(PortfolioDTO dto) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(dto.getId());
        portfolio.setPortfolioName(dto.getPortfolioName());
        portfolio.setDescription(dto.getDescription());
        portfolio.setTotalValue(dto.getTotalValue());
        portfolio.setBaseCurrency(dto.getBaseCurrency());
        portfolio.setInceptionDate(dto.getInceptionDate());
        return portfolio;
    }

    private PortfolioDTO convertToDTO(Portfolio portfolio) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setPortfolioName(portfolio.getPortfolioName());
        dto.setDescription(portfolio.getDescription());
        dto.setTotalValue(portfolio.getTotalValue());
        dto.setBaseCurrency(portfolio.getBaseCurrency());
        dto.setInceptionDate(portfolio.getInceptionDate());
        dto.setCreatedAt(portfolio.getCreatedAt());
        dto.setUpdatedAt(portfolio.getUpdatedAt());
        return dto;
    }

    private Holding convertToEntity(HoldingDTO dto) {
        Holding holding = new Holding();
        holding.setId(dto.getId());
        holding.setTickerSymbol(dto.getTickerSymbol());
        holding.setCompanyName(dto.getCompanyName());
        holding.setSector(dto.getSector());
        holding.setQuantity(dto.getQuantity());
        holding.setPurchasePrice(dto.getPurchasePrice());
        holding.setCurrentPrice(dto.getCurrentPrice());
        holding.setMarketValue(dto.getMarketValue());
        holding.setWeightInPortfolio(dto.getWeightInPortfolio());
        return holding;
    }

    private HoldingDTO convertToDTO(Holding holding) {
        HoldingDTO dto = new HoldingDTO();
        dto.setId(holding.getId());
        dto.setPortfolioId(holding.getPortfolio() != null ? holding.getPortfolio().getId() : null);
        dto.setTickerSymbol(holding.getTickerSymbol());
        dto.setCompanyName(holding.getCompanyName());
        dto.setSector(holding.getSector());
        dto.setQuantity(holding.getQuantity());
        dto.setPurchasePrice(holding.getPurchasePrice());
        dto.setCurrentPrice(holding.getCurrentPrice());
        dto.setMarketValue(holding.getMarketValue());
        dto.setWeightInPortfolio(holding.getWeightInPortfolio());
        dto.setCreatedAt(holding.getCreatedAt());
        dto.setUpdatedAt(holding.getUpdatedAt());
        
        // Calculate performance metrics
        dto.setUnrealizedGainLoss(holding.getUnrealizedGainLoss());
        dto.setUnrealizedGainLossPercentage(holding.getUnrealizedGainLossPercentage());
        
        return dto;
    }

    private EsgScore convertToEntity(EsgScoreDTO dto) {
        EsgScore esgScore = new EsgScore();
        esgScore.setId(dto.getId());
        esgScore.setScoreDate(dto.getScoreDate());
        esgScore.setOverallScore(dto.getOverallScore());
        esgScore.setEnvironmentalPillar(dto.getEnvironmentalPillar());
        esgScore.setSocialPillar(dto.getSocialPillar());
        esgScore.setGovernancePillar(dto.getGovernancePillar());
        
        if (dto.getControversyLevel() != null) {
            esgScore.setControversyLevel(EsgScore.ControversyLevel.valueOf(dto.getControversyLevel()));
        }
        
        esgScore.setDataProvider(dto.getDataProvider());
        return esgScore;
    }

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

    private RiskMetric convertToEntity(RiskMetricDTO dto) {
        RiskMetric riskMetric = new RiskMetric();
        riskMetric.setId(dto.getId());
        riskMetric.setCalculationDate(dto.getCalculationDate());
        riskMetric.setBeta(dto.getBeta());
        riskMetric.setVolatility(dto.getVolatility());
        riskMetric.setValueAtRisk(dto.getValueAtRisk());
        riskMetric.setMaxDrawdown(dto.getMaxDrawdown());
        riskMetric.setSharpeRatio(dto.getSharpeRatio());
        
        if (dto.getRiskRating() != null) {
            riskMetric.setRiskRating(RiskMetric.RiskRating.valueOf(dto.getRiskRating()));
        }
        
        riskMetric.setTimeHorizonDays(dto.getTimeHorizonDays());
        return riskMetric;
    }

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
