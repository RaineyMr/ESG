package com.morganstanley.esg.repository;

import com.morganstanley.esg.model.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Portfolio entity operations.
 * 
 * Provides data access methods for portfolio management including
 * custom queries for portfolio analytics and reporting.
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * Find portfolios by name (case-insensitive).
     */
    List<Portfolio> findByPortfolioNameIgnoreCase(String portfolioName);

    /**
     * Find portfolios by name containing the search string (case-insensitive).
     */
    List<Portfolio> findByPortfolioNameContainingIgnoreCase(String portfolioName);

    /**
     * Find portfolios by base currency.
     */
    List<Portfolio> findByBaseCurrency(String baseCurrency);

    /**
     * Find portfolios created within a date range.
     */
    List<Portfolio> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find portfolios with total value greater than or equal to the specified amount.
     */
    List<Portfolio> findByTotalValueGreaterThanEqual(BigDecimal minValue);

    /**
     * Find portfolios with total value within a range.
     */
    List<Portfolio> findByTotalValueBetween(BigDecimal minValue, BigDecimal maxValue);

    /**
     * Find portfolios by inception date.
     */
    List<Portfolio> findByInceptionDate(LocalDateTime inceptionDate);

    /**
     * Find portfolios with inception date within a range.
     */
    List<Portfolio> findByInceptionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count portfolios by base currency.
     */
    long countByBaseCurrency(String baseCurrency);

    /**
     * Check if a portfolio name exists (case-insensitive).
     */
    boolean existsByPortfolioNameIgnoreCase(String portfolioName);

    /**
     * Find portfolios with their holdings count.
     */
    @Query("SELECT p, COUNT(h) as holdingCount FROM Portfolio p " +
           "LEFT JOIN p.holdings h " +
           "GROUP BY p.id " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findPortfoliosWithHoldingCount();

    /**
     * Find portfolios with total value and holding count.
     */
    @Query("SELECT p.id, p.portfolioName, p.totalValue, p.baseCurrency, " +
           "p.inceptionDate, p.createdAt, COUNT(h) as holdingCount " +
           "FROM Portfolio p " +
           "LEFT JOIN p.holdings h " +
           "GROUP BY p.id, p.portfolioName, p.totalValue, p.baseCurrency, " +
           "p.inceptionDate, p.createdAt " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findPortfolioSummaries();

    /**
     * Find portfolios with average ESG scores.
     */
    @Query("SELECT p.id, p.portfolioName, p.totalValue, " +
           "COALESCE(AVG(es.overallScore), 0) as avgEsgScore, " +
           "COUNT(h) as holdingCount " +
           "FROM Portfolio p " +
           "LEFT JOIN p.holdings h " +
           "LEFT JOIN h.esgScores es " +
           "WHERE es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = h.id" +
           ") " +
           "GROUP BY p.id, p.portfolioName, p.totalValue " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findPortfolioEsgSummaries();

    /**
     * Find portfolios with risk metrics.
     */
    @Query("SELECT p.id, p.portfolioName, p.totalValue, " +
           "COALESCE(AVG(rm.beta), 0) as avgBeta, " +
           "COALESCE(AVG(rm.volatility), 0) as avgVolatility, " +
           "MAX(rm.riskRating) as topRiskRating, " +
           "COUNT(h) as holdingCount " +
           "FROM Portfolio p " +
           "LEFT JOIN p.holdings h " +
           "LEFT JOIN h.riskMetrics rm " +
           "WHERE rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ") " +
           "GROUP BY p.id, p.portfolioName, p.totalValue " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findPortfolioRiskSummaries();

    /**
     * Complete portfolio summary with ESG and risk metrics.
     */
    @Query("SELECT p.id, p.portfolioName, p.totalValue, p.baseCurrency, " +
           "p.inceptionDate, p.createdAt, p.updatedAt, " +
           "COUNT(h) as holdingCount, " +
           "COALESCE(AVG(es.overallScore), 0) as avgEsgScore, " +
           "COALESCE(MAX(rm.riskRating), 'UNKNOWN') as topRiskRating, " +
           "COALESCE(AVG(rm.beta), 0) as avgBeta, " +
           "COALESCE(AVG(rm.volatility), 0) as avgVolatility " +
           "FROM Portfolio p " +
           "LEFT JOIN p.holdings h " +
           "LEFT JOIN h.esgScores es ON es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = h.id" +
           ") " +
           "LEFT JOIN h.riskMetrics rm ON rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ") " +
           "GROUP BY p.id, p.portfolioName, p.totalValue, p.baseCurrency, " +
           "p.inceptionDate, p.createdAt, p.updatedAt " +
           "ORDER BY p.createdAt DESC")
    List<Object[]> findCompletePortfolioSummaries();

    /**
     * Find portfolio by ID with all related data.
     */
    @Query("SELECT p FROM Portfolio p " +
           "LEFT JOIN FETCH p.holdings h " +
           "LEFT JOIN FETCH h.esgScores es " +
           "LEFT JOIN FETCH h.riskMetrics rm " +
           "LEFT JOIN FETCH p.esgMetrics em " +
           "WHERE p.id = :portfolioId")
    Optional<Portfolio> findPortfolioWithDetails(@Param("portfolioId") Long portfolioId);

    /**
     * Search portfolios by multiple criteria.
     */
    @Query("SELECT p FROM Portfolio p WHERE " +
           "(:portfolioName IS NULL OR LOWER(p.portfolioName) LIKE LOWER(CONCAT('%', :portfolioName, '%'))) AND " +
           "(:baseCurrency IS NULL OR p.baseCurrency = :baseCurrency) AND " +
           "(:minValue IS NULL OR p.totalValue >= :minValue) AND " +
           "(:maxValue IS NULL OR p.totalValue <= :maxValue)")
    Page<Portfolio> searchPortfolios(@Param("portfolioName") String portfolioName,
                                    @Param("baseCurrency") String baseCurrency,
                                    @Param("minValue") BigDecimal minValue,
                                    @Param("maxValue") BigDecimal maxValue,
                                    Pageable pageable);

    /**
     * Get portfolio statistics.
     */
    @Query("SELECT COUNT(p) as totalPortfolios, " +
           "SUM(p.totalValue) as totalValue, " +
           "AVG(p.totalValue) as avgValue, " +
           "MIN(p.totalValue) as minValue, " +
           "MAX(p.totalValue) as maxValue " +
           "FROM Portfolio p")
    Object[] getPortfolioStatistics();

    /**
     * Find portfolios created in the last N days.
     */
    @Query("SELECT p FROM Portfolio p WHERE p.createdAt >= :sinceDate")
    List<Portfolio> findPortfoliosCreatedSince(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find portfolios with no holdings.
     */
    @Query("SELECT p FROM Portfolio p WHERE SIZE(p.holdings) = 0")
    List<Portfolio> findPortfoliosWithoutHoldings();

    /**
     * Find portfolios with holdings count greater than specified threshold.
     */
    @Query("SELECT p FROM Portfolio p WHERE SIZE(p.holdings) > :holdingCount")
    List<Portfolio> findPortfoliosWithHoldingCountGreaterThan(@Param("holdingCount") int holdingCount);

    /**
     * Get portfolio value distribution by currency.
     */
    @Query("SELECT p.baseCurrency, COUNT(p) as count, SUM(p.totalValue) as totalValue " +
           "FROM Portfolio p " +
           "GROUP BY p.baseCurrency " +
           "ORDER BY totalValue DESC")
    List<Object[]> getPortfolioValueDistributionByCurrency();
}
