package com.morganstanley.esg.repository;

import com.morganstanley.esg.model.EsgScore;
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
 * Repository interface for EsgScore entity operations.
 * 
 * Provides data access methods for ESG score management including
 * custom queries for ESG analytics and trend analysis.
 */
@Repository
public interface EsgScoreRepository extends JpaRepository<EsgScore, Long> {

    /**
     * Find ESG scores by holding.
     */
    List<EsgScore> findByHoldingId(Long holdingId);

    /**
     * Find ESG scores by holding with pagination.
     */
    Page<EsgScore> findByHoldingId(Long holdingId, Pageable pageable);

    /**
     * Find ESG scores by score date.
     */
    List<EsgScore> findByScoreDate(LocalDate scoreDate);

    /**
     * Find ESG scores by date range.
     */
    List<EsgScore> findByScoreDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find ESG scores by controversy level.
     */
    List<EsgScore> findByControversyLevel(EsgScore.ControversyLevel controversyLevel);

    /**
     * Find ESG scores by data provider.
     */
    List<EsgScore> findByDataProvider(String dataProvider);

    /**
     * Find ESG scores with overall score greater than or equal to the specified value.
     */
    List<EsgScore> findByOverallScoreGreaterThanEqual(BigDecimal minScore);

    /**
     * Find ESG scores with overall score within a range.
     */
    List<EsgScore> findByOverallScoreBetween(BigDecimal minScore, BigDecimal maxScore);

    /**
     * Find latest ESG score for a holding.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.holding.id = :holdingId " +
           "ORDER BY es.scoreDate DESC")
    Optional<EsgScore> findLatestByHoldingId(@Param("holdingId") Long holdingId);

    /**
     * Find latest ESG score for multiple holdings.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.holding.id IN :holdingIds " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ")")
    List<EsgScore> findLatestByHoldingIds(@Param("holdingIds") List<Long> holdingIds);

    /**
     * Find ESG scores for a portfolio's holdings.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "ORDER BY es.scoreDate DESC, es.holding.companyName")
    List<EsgScore> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Find latest ESG scores for a portfolio's holdings.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ") " +
           "ORDER BY es.overallScore DESC")
    List<EsgScore> findLatestByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Get ESG score trend for a holding.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.holding.id = :holdingId " +
           "AND es.scoreDate BETWEEN :startDate AND :endDate " +
           "ORDER BY es.scoreDate ASC")
    List<EsgScore> findEsgTrendForHolding(@Param("holdingId") Long holdingId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * Get ESG score distribution by rating categories.
     */
    @Query("SELECT CASE " +
           "    WHEN es.overallScore >= 80 THEN 'AAA' " +
           "    WHEN es.overallScore >= 70 THEN 'AA' " +
           "    WHEN es.overallScore >= 60 THEN 'A' " +
           "    WHEN es.overallScore >= 50 THEN 'BBB' " +
           "    WHEN es.overallScore >= 40 THEN 'BB' " +
           "    WHEN es.overallScore >= 30 THEN 'B' " +
           "    WHEN es.overallScore >= 20 THEN 'CCC' " +
           "    ELSE 'D' " +
           "END as rating, COUNT(es) as count " +
           "FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ") " +
           "GROUP BY rating " +
           "ORDER BY rating")
    List<Object[]> getEsgRatingDistribution(@Param("portfolioId") Long portfolioId);

    /**
     * Get ESG pillar averages for a portfolio.
     */
    @Query("SELECT AVG(es.environmentalPillar), AVG(es.socialPillar), AVG(es.governancePillar), AVG(es.overallScore) " +
           "FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ")")
    Object[] getEsgPillarAverages(@Param("portfolioId") Long portfolioId);

    /**
     * Find holdings with high ESG scores.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.overallScore >= :minScore " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ") " +
           "ORDER BY es.overallScore DESC")
    List<EsgScore> findHoldingsWithHighEsgScores(@Param("minScore") BigDecimal minScore);

    /**
     * Find holdings with controversy issues.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.controversyLevel IN :controversyLevels " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ") " +
           "ORDER BY es.controversyLevel DESC")
    List<EsgScore> findHoldingsWithControversies(@Param("controversyLevels") List<EsgScore.ControversyLevel> controversyLevels);

    /**
     * Get ESG score changes over time for a portfolio.
     */
    @Query("SELECT es.scoreDate, AVG(es.overallScore) " +
           "FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "GROUP BY es.scoreDate " +
           "ORDER BY es.scoreDate ASC")
    List<Object[]> getPortfolioEsgTrend(@Param("portfolioId") Long portfolioId);

    /**
     * Search ESG scores by multiple criteria.
     */
    @Query("SELECT es FROM EsgScore es WHERE " +
           "(:holdingId IS NULL OR es.holding.id = :holdingId) AND " +
           "(:startDate IS NULL OR es.scoreDate >= :startDate) AND " +
           "(:endDate IS NULL OR es.scoreDate <= :endDate) AND " +
           "(:minOverallScore IS NULL OR es.overallScore >= :minOverallScore) AND " +
           "(:maxOverallScore IS NULL OR es.overallScore <= :maxOverallScore) AND " +
           "(:controversyLevel IS NULL OR es.controversyLevel = :controversyLevel) AND " +
           "(:dataProvider IS NULL OR es.dataProvider = :dataProvider)")
    Page<EsgScore> searchEsgScores(@Param("holdingId") Long holdingId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("minOverallScore") BigDecimal minOverallScore,
                                  @Param("maxOverallScore") BigDecimal maxOverallScore,
                                  @Param("controversyLevel") EsgScore.ControversyLevel controversyLevel,
                                  @Param("dataProvider") String dataProvider,
                                  Pageable pageable);

    /**
     * Get ESG statistics for a portfolio.
     */
    @Query("SELECT COUNT(es) as totalScores, " +
           "AVG(es.overallScore) as avgOverallScore, " +
           "MIN(es.overallScore) as minOverallScore, " +
           "MAX(es.overallScore) as maxOverallScore, " +
           "AVG(es.environmentalPillar) as avgEnvironmental, " +
           "AVG(es.socialPillar) as avgSocial, " +
           "AVG(es.governancePillar) as avgGovernance " +
           "FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ")")
    Object[] getEsgStatistics(@Param("portfolioId") Long portfolioId);

    /**
     * Find ESG scores by sector for a portfolio.
     */
    @Query("SELECT h.sector, AVG(es.overallScore), COUNT(es) " +
           "FROM EsgScore es " +
           "JOIN es.holding h " +
           "WHERE h.portfolio.id = :portfolioId " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = h.id" +
           ") " +
           "GROUP BY h.sector " +
           "ORDER BY AVG(es.overallScore) DESC")
    List<Object[]> getEsgScoresBySector(@Param("portfolioId") Long portfolioId);

    /**
     * Count ESG scores by controversy level for a portfolio.
     */
    @Query("SELECT es.controversyLevel, COUNT(es) " +
           "FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ") " +
           "GROUP BY es.controversyLevel " +
           "ORDER BY COUNT(es) DESC")
    List<Object[]> countByControversyLevel(@Param("portfolioId") Long portfolioId);

    /**
     * Get ESG score improvement candidates (holdings with potential for improvement).
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.overallScore < :threshold " +
           "AND es.scoreDate = (" +
           "    SELECT MAX(es2.scoreDate) FROM EsgScore es2 WHERE es2.holding.id = es.holding.id" +
           ") " +
           "ORDER BY es.overallScore ASC")
    List<EsgScore> findImprovementCandidates(@Param("threshold") BigDecimal threshold);

    /**
     * Find ESG scores for a specific date across all holdings.
     */
    @Query("SELECT es FROM EsgScore es " +
           "WHERE es.scoreDate = :date " +
           "ORDER BY es.overallScore DESC")
    List<EsgScore> findScoresByDate(@Param("date") LocalDate date);

    /**
     * Get latest ESG score date for a portfolio.
     */
    @Query("SELECT MAX(es.scoreDate) " +
           "FROM EsgScore es " +
           "WHERE es.holding.portfolio.id = :portfolioId")
    Optional<LocalDate> getLatestScoreDate(@Param("portfolioId") Long portfolioId);
}
