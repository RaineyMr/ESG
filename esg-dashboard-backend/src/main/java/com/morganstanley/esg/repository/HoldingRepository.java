package com.morganstanley.esg.repository;

import com.morganstanley.esg.model.Holding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Holding entity operations.
 * 
 * Provides data access methods for holding management including
 * custom queries for holding analytics and portfolio analysis.
 */
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    /**
     * Find holdings by portfolio.
     */
    List<Holding> findByPortfolioId(Long portfolioId);

    /**
     * Find holdings by portfolio with pagination.
     */
    Page<Holding> findByPortfolioId(Long portfolioId, Pageable pageable);

    /**
     * Find holdings by ticker symbol.
     */
    List<Holding> findByTickerSymbol(String tickerSymbol);

    /**
     * Find holdings by ticker symbol (case-insensitive).
     */
    List<Holding> findByTickerSymbolIgnoreCase(String tickerSymbol);

    /**
     * Find holdings by company name (case-insensitive).
     */
    List<Holding> findByCompanyNameIgnoreCase(String companyName);

    /**
     * Find holdings by company name containing the search string (case-insensitive).
     */
    List<Holding> findByCompanyNameContainingIgnoreCase(String companyName);

    /**
     * Find holdings by sector.
     */
    List<Holding> findBySector(String sector);

    /**
     * Find holdings by sector (case-insensitive).
     */
    List<Holding> findBySectorIgnoreCase(String sector);

    /**
     * Find holdings with market value greater than or equal to the specified amount.
     */
    List<Holding> findByMarketValueGreaterThanEqual(BigDecimal minValue);

    /**
     * Find holdings with market value within a range.
     */
    List<Holding> findByMarketValueBetween(BigDecimal minValue, BigDecimal maxValue);

    /**
     * Find holdings with weight in portfolio greater than or equal to the specified amount.
     */
    List<Holding> findByWeightInPortfolioGreaterThanEqual(BigDecimal minWeight);

    /**
     * Find holdings by portfolio and sector.
     */
    List<Holding> findByPortfolioIdAndSector(Long portfolioId, String sector);

    /**
     * Count holdings by portfolio.
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * Count holdings by sector.
     */
    long countBySector(String sector);

    /**
     * Sum of market values by portfolio.
     */
    @Query("SELECT SUM(h.marketValue) FROM Holding h WHERE h.portfolio.id = :portfolioId")
    BigDecimal sumMarketValueByPortfolio(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with their latest ESG scores.
     */
    @Query("SELECT h, es FROM Holding h " +
           "LEFT JOIN h.esgScores es " +
           "WHERE es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = h.id" +
           ") " +
           "AND h.portfolio.id = :portfolioId")
    List<Object[]> findHoldingsWithLatestEsgScores(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with their latest risk metrics.
     */
    @Query("SELECT h, rm FROM Holding h " +
           "LEFT JOIN h.riskMetrics rm " +
           "WHERE rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ") " +
           "AND h.portfolio.id = :portfolioId")
    List<Object[]> findHoldingsWithLatestRiskMetrics(@Param("portfolioId") Long portfolioId);

    /**
     * Complete holding data with latest ESG and risk metrics.
     */
    @Query("SELECT h, es, rm FROM Holding h " +
           "LEFT JOIN h.esgScores es ON es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = h.id" +
           ") " +
           "LEFT JOIN h.riskMetrics rm ON rm.calculationDate = (" +
           "    SELECT MAX(rm2.calculationDate) FROM RiskMetric rm2 WHERE rm2.holding.id = h.id" +
           ") " +
           "WHERE h.portfolio.id = :portfolioId " +
           "ORDER BY h.marketValue DESC")
    List<Object[]> findHoldingsWithCompleteData(@Param("portfolioId") Long portfolioId);

    /**
     * Search holdings by multiple criteria.
     */
    @Query("SELECT h FROM Holding h WHERE " +
           "(:portfolioId IS NULL OR h.portfolio.id = :portfolioId) AND " +
           "(:tickerSymbol IS NULL OR LOWER(h.tickerSymbol) LIKE LOWER(CONCAT('%', :tickerSymbol, '%'))) AND " +
           "(:companyName IS NULL OR LOWER(h.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
           "(:sector IS NULL OR h.sector = :sector) AND " +
           "(:minMarketValue IS NULL OR h.marketValue >= :minMarketValue) AND " +
           "(:maxMarketValue IS NULL OR h.marketValue <= :maxMarketValue)")
    Page<Holding> searchHoldings(@Param("portfolioId") Long portfolioId,
                                @Param("tickerSymbol") String tickerSymbol,
                                @Param("companyName") String companyName,
                                @Param("sector") String sector,
                                @Param("minMarketValue") BigDecimal minMarketValue,
                                @Param("maxMarketValue") BigDecimal maxMarketValue,
                                Pageable pageable);

    /**
     * Get sector distribution for a portfolio.
     */
    @Query("SELECT h.sector, COUNT(h) as count, SUM(h.marketValue) as totalValue, " +
           "AVG(h.weightInPortfolio) as avgWeight " +
           "FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "GROUP BY h.sector " +
           "ORDER BY totalValue DESC")
    List<Object[]> getSectorDistributionByPortfolio(@Param("portfolioId") Long portfolioId);

    /**
     * Get top holdings by market value for a portfolio.
     */
    @Query("SELECT h FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "ORDER BY h.marketValue DESC")
    List<Holding> getTopHoldingsByMarketValue(@Param("portfolioId") Long portfolioId, Pageable pageable);

    /**
     * Get holdings with highest weight in portfolio.
     */
    @Query("SELECT h FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "ORDER BY h.weightInPortfolio DESC")
    List<Holding> getTopHoldingsByWeight(@Param("portfolioId") Long portfolioId, Pageable pageable);

    /**
     * Find holdings with unrealized gains.
     */
    @Query("SELECT h FROM Holding h " +
           "WHERE h.currentPrice > h.purchasePrice " +
           "AND h.portfolio.id = :portfolioId")
    List<Holding> findHoldingsWithGains(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with unrealized losses.
     */
    @Query("SELECT h FROM Holding h " +
           "WHERE h.currentPrice < h.purchasePrice " +
           "AND h.portfolio.id = :portfolioId")
    List<Holding> findHoldingsWithLosses(@Param("portfolioId") Long portfolioId);

    /**
     * Calculate total unrealized gain/loss for a portfolio.
     */
    @Query("SELECT SUM((h.currentPrice - h.purchasePrice) * h.quantity) " +
           "FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId")
    BigDecimal calculateTotalUnrealizedGainLoss(@Param("portfolioId") Long portfolioId);

    /**
     * Get holding statistics for a portfolio.
     */
    @Query("SELECT COUNT(h) as totalHoldings, " +
           "SUM(h.marketValue) as totalMarketValue, " +
           "AVG(h.marketValue) as avgMarketValue, " +
           "MIN(h.marketValue) as minMarketValue, " +
           "MAX(h.marketValue) as maxMarketValue, " +
           "AVG(h.weightInPortfolio) as avgWeight " +
           "FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId")
    Object[] getHoldingStatisticsByPortfolio(@Param("portfolioId") Long portfolioId);

    /**
     * Find duplicate holdings (same ticker in same portfolio).
     */
    @Query("SELECT h.tickerSymbol, h.portfolio.id, COUNT(h) as count " +
           "FROM Holding h " +
           "GROUP BY h.tickerSymbol, h.portfolio.id " +
           "HAVING COUNT(h) > 1")
    List<Object[]> findDuplicateHoldings();

    /**
     * Get holdings by performance (percentage gain/loss).
     */
    @Query("SELECT h, ((h.currentPrice - h.purchasePrice) / h.purchasePrice * 100) as percentageChange " +
           "FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "ORDER BY percentageChange DESC")
    List<Object[]> getHoldingsByPerformance(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with no ESG scores.
     */
    @Query("SELECT h FROM Holding h " +
           "WHERE SIZE(h.esgScores) = 0 " +
           "AND h.portfolio.id = :portfolioId")
    List<Holding> findHoldingsWithoutEsgScores(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with no risk metrics.
     */
    @Query("SELECT h FROM Holding h " +
           "WHERE SIZE(h.riskMetrics) = 0 " +
           "AND h.portfolio.id = :portfolioId")
    List<Holding> findHoldingsWithoutRiskMetrics(@Param("portfolioId") Long portfolioId);

    /**
     * Get portfolio concentration (top 10 holdings percentage).
     */
    @Query("SELECT SUM(h.weightInPortfolio) " +
           "FROM Holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "ORDER BY h.weightInPortfolio DESC " +
           "LIMIT 10")
    BigDecimal getPortfolioConcentration(@Param("portfolioId") Long portfolioId);
}
