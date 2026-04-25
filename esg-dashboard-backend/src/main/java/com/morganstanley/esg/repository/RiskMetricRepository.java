package com.morganstanley.esg.repository;

import com.morganstanley.esg.model.RiskMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RiskMetric entity operations.
 * 
 * Provides data access methods for risk metric management including
 * custom queries for risk analysis and portfolio risk assessment.
 */
@Repository
public interface RiskMetricRepository extends JpaRepository<RiskMetric, Long> {

    /**
     * Find risk metrics by holding.
     */
    List<RiskMetric> findByHoldingId(Long holdingId);

    /**
     * Find risk metrics by holding with pagination.
     */
    Page<RiskMetric> findByHoldingId(Long holdingId, Pageable pageable);

    /**
     * Find risk metrics by calculation date.
     */
    List<RiskMetric> findByCalculationDate(LocalDate calculationDate);

    /**
     * Find risk metrics by date range.
     */
    List<RiskMetric> findByCalculationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find risk metrics by risk rating.
     */
    List<RiskMetric> findByRiskRating(RiskMetric.RiskRating riskRating);

    /**
     * Find risk metrics with beta greater than or equal to the specified value.
     */
    List<RiskMetric> findByBetaGreaterThanEqual(BigDecimal minBeta);

    /**
     * Find risk metrics with volatility greater than or equal to the specified value.
     */
    List<RiskMetric> findByVolatilityGreaterThanEqual(BigDecimal minVolatility);

    /**
     * Find risk metrics with Sharpe ratio greater than or equal to the specified value.
     */
    List<RiskMetric> findBySharpeRatioGreaterThanEqual(BigDecimal minSharpeRatio);

    /**
     * Find latest risk metric for a holding.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.holding.id = :holdingId " +
           "ORDER BY rm.calculationDate DESC")
    Optional<RiskMetric> findLatestByHoldingId(@Param("holdingId") Long holdingId);

    /**
     * Find latest risk metrics for multiple holdings.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.holding.id IN :holdingIds " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ")")
    List<RiskMetric> findLatestByHoldingIds(@Param("holdingIds") List<Long> holdingIds);

    /**
     * Find risk metrics for a portfolio's holdings.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "ORDER BY rm.calculationDate DESC, rm.holding.companyName")
    List<RiskMetric> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Find latest risk metrics for a portfolio's holdings.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "ORDER BY rm.riskRating DESC")
    List<RiskMetric> findLatestByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Get risk trend for a holding.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.holding.id = :holdingId " +
           "AND rm.calculationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY rm.calculationDate ASC")
    List<RiskMetric> findRiskTrendForHolding(@Param("holdingId") Long holdingId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Get risk rating distribution for a portfolio.
     */
    @Query("SELECT rm.riskRating, COUNT(rm) as count " +
           "FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "GROUP BY rm.riskRating " +
           "ORDER BY COUNT(rm) DESC")
    List<Object[]> getRiskRatingDistribution(@Param("portfolioId") Long portfolioId);

    /**
     * Get risk averages for a portfolio.
     */
    @Query("SELECT AVG(rm.beta), AVG(rm.volatility), AVG(rm.valueAtRisk), " +
           "AVG(rm.maxDrawdown), AVG(rm.sharpeRatio) " +
           "FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ")")
    Object[] getRiskAverages(@Param("portfolioId") Long portfolioId);

    /**
     * Find high-risk holdings.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.riskRating IN :riskRatings " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "ORDER BY rm.riskRating DESC")
    List<RiskMetric> findHighRiskHoldings(@Param("riskRatings") List<RiskMetric.RiskRating> riskRatings);

    /**
     * Find holdings with high beta.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE ABS(rm.beta) >= :minBeta " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "ORDER BY ABS(rm.beta) DESC")
    List<RiskMetric> findHighBetaHoldings(@Param("minBeta") BigDecimal minBeta);

    /**
     * Find holdings with high volatility.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.volatility >= :minVolatility " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "ORDER BY rm.volatility DESC")
    List<RiskMetric> findHighVolatilityHoldings(@Param("minVolatility") BigDecimal minVolatility);

    /**
     * Find holdings with negative Sharpe ratio.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.sharpeRatio < 0 " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "ORDER BY rm.sharpeRatio ASC")
    List<RiskMetric> findNegativeSharpeHoldings();

    /**
     * Search risk metrics by multiple criteria.
     */
    @Query("SELECT rm FROM RiskMetric rm WHERE " +
           "(:holdingId IS NULL OR rm.holding.id = :holdingId) AND " +
           "(:startDate IS NULL OR rm.calculationDate >= :startDate) AND " +
           "(:endDate IS NULL OR rm.calculationDate <= :endDate) AND " +
           "(:minBeta IS NULL OR ABS(rm.beta) >= :minBeta) AND " +
           "(:maxBeta IS NULL OR ABS(rm.beta) <= :maxBeta) AND " +
           "(:minVolatility IS NULL OR rm.volatility >= :minVolatility) AND " +
           "(:maxVolatility IS NULL OR rm.volatility <= :maxVolatility) AND " +
           "(:riskRating IS NULL OR rm.riskRating = :riskRating)")
    Page<RiskMetric> searchRiskMetrics(@Param("holdingId") Long holdingId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("minBeta") BigDecimal minBeta,
                                      @Param("maxBeta") BigDecimal maxBeta,
                                      @Param("minVolatility") BigDecimal minVolatility,
                                      @Param("maxVolatility") BigDecimal maxVolatility,
                                      @Param("riskRating") RiskMetric.RiskRating riskRating,
                                      Pageable pageable);

    /**
     * Get risk statistics for a portfolio.
     */
    @Query("SELECT COUNT(rm) as totalMetrics, " +
           "AVG(rm.beta) as avgBeta, " +
           "AVG(rm.volatility) as avgVolatility, " +
           "AVG(rm.valueAtRisk) as avgValueAtRisk, " +
           "AVG(rm.maxDrawdown) as avgMaxDrawdown, " +
           "AVG(rm.sharpeRatio) as avgSharpeRatio " +
           "FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ")")
    Object[] getRiskStatistics(@Param("portfolioId") Long portfolioId);

    /**
     * Get risk metrics by sector for a portfolio.
     */
    @Query("SELECT h.sector, AVG(rm.beta), AVG(rm.volatility), AVG(rm.sharpeRatio), COUNT(rm) " +
           "FROM RiskMetric rm " +
           "JOIN rm.holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ") " +
           "GROUP BY h.sector " +
           "ORDER BY AVG(rm.volatility) DESC")
    List<Object[]> getRiskMetricsBySector(@Param("portfolioId") Long portfolioId);

    /**
     * Count risk metrics by risk rating for a portfolio.
     */
    @Query("SELECT rm.riskRating, COUNT(rm) " +
           "FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ") " +
           "GROUP BY rm.riskRating " +
           "ORDER BY COUNT(rm) DESC")
    List<Object[]> countByRiskRating(@Param("portfolioId") Long portfolioId);

    /**
     * Get portfolio risk score (composite risk metric).
     */
    @Query("SELECT (COUNT(CASE WHEN rm.riskRating = 'VERY_HIGH' THEN 1 END) * 4 + " +
           "COUNT(CASE WHEN rm.riskRating = 'HIGH' THEN 1 END) * 3 + " +
           "COUNT(CASE WHEN rm.riskRating = 'MODERATE' THEN 1 END) * 2 + " +
           "COUNT(CASE WHEN rm.riskRating = 'LOW' THEN 1 END) * 1) / " +
           "NULLIF(COUNT(rm), 0) " +
           "FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = rm.holding.id" +
           ")")
    BigDecimal calculatePortfolioRiskScore(@Param("portfolioId") Long portfolioId);

    /**
     * Find risk metrics for a specific date across all holdings.
     */
    @Query("SELECT rm FROM RiskMetric rm " +
           "WHERE rm.calculationDate = :date " +
           "ORDER BY rm.volatility DESC")
    List<RiskMetric> findMetricsByDate(@Param("date") LocalDate date);

    /**
     * Get latest risk metric date for a portfolio.
     */
    @Query("SELECT MAX(rm.calculationDate) " +
           "FROM RiskMetric rm " +
           "WHERE rm.holding.portfolio.id = :portfolioId")
    Optional<LocalDate> getLatestMetricDate(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with improving risk metrics.
     */
    @Query("SELECT current.holding.id, current.holding.companyName, " +
           "current.riskRating as currentRating, previous.riskRating as previousRating " +
           "FROM RiskMetric current " +
           "JOIN RiskMetric previous ON current.holding.id = previous.holding.id " +
           "WHERE current.calculationDate = :currentDate " +
           "AND previous.calculationDate = :previousDate " +
           "AND current.riskRating < previous.riskRating " +
           "ORDER BY current.riskRating ASC")
    List<Object[]> findImprovingRiskMetrics(@Param("currentDate") LocalDate currentDate,
                                           @Param("previousDate") LocalDate previousDate);

    /**
     * Get portfolio beta (weighted average).
     */
    @Query("SELECT SUM(rm.beta * h.weightInPortfolio) " +
           "FROM RiskMetric rm " +
           "JOIN rm.holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ")")
    BigDecimal calculatePortfolioBeta(@Param("portfolioId") Long portfolioId);

    /**
     * Get portfolio VaR (weighted average).
     */
    @Query("SELECT SUM(rm.valueAtRisk * h.weightInPortfolio) " +
           "FROM RiskMetric rm " +
           "JOIN rm.holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "AND rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ")")
    BigDecimal calculatePortfolioValueAtRisk(@Param("portfolioId") Long portfolioId);

    /**
     * Count risk metrics by portfolio.
     */
    long countByHoldingPortfolioId(Long portfolioId);
}
