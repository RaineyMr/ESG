package com.morganstanley.esg.repository;

import com.morganstanley.esg.model.EsgMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EsgMetric entity operations.
 * 
 * Provides data access methods for portfolio-level ESG metrics including
 * custom queries for ESG trend analysis and portfolio performance.
 */
@Repository
public interface EsgMetricRepository extends JpaRepository<EsgMetric, Long> {

    /**
     * Find ESG metrics by portfolio.
     */
    List<EsgMetric> findByPortfolioId(Long portfolioId);

    /**
     * Find ESG metrics by portfolio ordered by date.
     */
    List<EsgMetric> findByPortfolioIdOrderByMetricDateDesc(Long portfolioId);

    /**
     * Find ESG metrics by metric date.
     */
    List<EsgMetric> findByMetricDate(LocalDate metricDate);

    /**
     * Find ESG metrics by date range.
     */
    List<EsgMetric> findByMetricDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find ESG metrics by portfolio and date range.
     */
    List<EsgMetric> findByPortfolioIdAndMetricDateBetween(Long portfolioId, LocalDate startDate, LocalDate endDate);

    /**
     * Find latest ESG metric for a portfolio.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.portfolio.id = :portfolioId " +
           "ORDER BY em.metricDate DESC")
    Optional<EsgMetric> findLatestByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Find ESG metric for a portfolio on a specific date.
     */
    Optional<EsgMetric> findByPortfolioIdAndMetricDate(Long portfolioId, LocalDate metricDate);

    /**
     * Get ESG trend for a portfolio.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.portfolio.id = :portfolioId " +
           "AND em.metricDate BETWEEN :startDate AND :endDate " +
           "ORDER BY em.metricDate ASC")
    List<EsgMetric> findEsgTrendForPortfolio(@Param("portfolioId") Long portfolioId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Get ESG performance comparison across portfolios.
     */
    @Query("SELECT em.portfolio.id, em.portfolio.portfolioName, em.overallEsgScore, em.metricDate " +
           "FROM EsgMetric em " +
           "WHERE em.metricDate = :date " +
           "ORDER BY em.overallEsgScore DESC")
    List<Object[]> getPortfolioEsgComparison(@Param("date") LocalDate date);

    /**
     * Find portfolios with high ESG scores.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.overallEsgScore >= :minScore " +
           "AND em.metricDate = (" +
           "    SELECT MAX(em2.metricDate) FROM EsgMetric em2 WHERE em2.portfolio.id = em.portfolio.id" +
           ") " +
           "ORDER BY em.overallEsgScore DESC")
    List<EsgMetric> findPortfoliosWithHighEsgScores(@Param("minScore") BigDecimal minScore);

    /**
     * Get ESG pillar averages across all portfolios.
     */
    @Query("SELECT AVG(em.environmentalScore), AVG(em.socialScore), AVG(em.governanceScore), AVG(em.overallEsgScore) " +
           "FROM EsgMetric em " +
           "WHERE em.metricDate = :date")
    Object[] getEsgPillarAveragesByDate(@Param("date") LocalDate date);

    /**
     * Get ESG score improvement over time for a portfolio.
     */
    @Query("SELECT em.metricDate, em.overallEsgScore " +
           "FROM EsgMetric em " +
           "WHERE em.portfolio.id = :portfolioId " +
           "ORDER BY em.metricDate ASC")
    List<Object[]> getEsgScoreProgression(@Param("portfolioId") Long portfolioId);

    /**
     * Find portfolios with controversy issues.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.controversyCount > :threshold " +
           "AND em.metricDate = (" +
           "    SELECT MAX(em2.metricDate) FROM EsgMetric em2 WHERE em2.portfolio.id = em.portfolio.id" +
           ") " +
           "ORDER BY em.controversyCount DESC")
    List<EsgMetric> findPortfoliosWithControversies(@Param("threshold") Integer threshold);

    /**
     * Get ESG statistics for a portfolio over time.
     */
    @Query("SELECT COUNT(em) as totalMetrics, " +
           "AVG(em.overallEsgScore) as avgOverallScore, " +
           "MIN(em.overallEsgScore) as minOverallScore, " +
           "MAX(em.overallEsgScore) as maxOverallScore, " +
           "AVG(em.controversyCount) as avgControversyCount " +
           "FROM EsgMetric em " +
           "WHERE em.portfolio.id = :portfolioId")
    Object[] getEsgStatistics(@Param("portfolioId") Long portfolioId);

    /**
     * Find ESG metrics for a specific date across all portfolios.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.metricDate = :date " +
           "ORDER BY em.overallEsgScore DESC")
    List<EsgMetric> findMetricsByDate(@Param("date") LocalDate date);

    /**
     * Get latest ESG metric date for a portfolio.
     */
    @Query("SELECT MAX(em.metricDate) " +
           "FROM EsgMetric em " +
           "WHERE em.portfolio.id = :portfolioId")
    Optional<LocalDate> getLatestMetricDate(@Param("portfolioId") Long portfolioId);

    /**
     * Count ESG metrics by portfolio.
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * Find portfolios with ESG scores above average.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.overallEsgScore > (" +
           "    SELECT AVG(em2.overallEsgScore) FROM EsgMetric em2 " +
           "    WHERE em2.metricDate = em.metricDate" +
           ") " +
           "AND em.metricDate = :date")
    List<EsgMetric> findAboveAveragePortfolios(@Param("date") LocalDate date);

    /**
     * Get ESG score distribution by rating categories.
     */
    @Query("SELECT CASE " +
           "    WHEN em.overallEsgScore >= 80 THEN 'EXCELLENT' " +
           "    WHEN em.overallEsgScore >= 60 THEN 'GOOD' " +
           "    WHEN em.overallEsgScore >= 40 THEN 'AVERAGE' " +
           "    WHEN em.overallEsgScore >= 20 THEN 'BELOW_AVERAGE' " +
           "    ELSE 'POOR' " +
           "END as rating, COUNT(em) as count " +
           "FROM EsgMetric em " +
           "WHERE em.metricDate = :date " +
           "GROUP BY rating " +
           "ORDER BY rating")
    List<Object[]> getEsgRatingDistribution(@Param("date") LocalDate date);

    /**
     * Find ESG metrics with environmental focus.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.environmentalScore >= :minScore " +
           "AND em.metricDate = (" +
           "    SELECT MAX(em2.metricDate) FROM EsgMetric em2 WHERE em2.portfolio.id = em.portfolio.id" +
           ") " +
           "ORDER BY em.environmentalScore DESC")
    List<EsgMetric> findEnvironmentallyFocusedPortfolios(@Param("minScore") BigDecimal minScore);

    /**
     * Find ESG metrics with social focus.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.socialScore >= :minScore " +
           "AND em.metricDate = (" +
           "    SELECT MAX(em2.metricDate) FROM EsgMetric em2 WHERE em2.portfolio.id = em.portfolio.id" +
           ") " +
           "ORDER BY em.socialScore DESC")
    List<EsgMetric> findSociallyFocusedPortfolios(@Param("minScore") BigDecimal minScore);

    /**
     * Find ESG metrics with governance focus.
     */
    @Query("SELECT em FROM EsgMetric em " +
           "WHERE em.governanceScore >= :minScore " +
           "AND em.metricDate = (" +
           "    SELECT MAX(em2.metricDate) FROM EsgMetric em2 WHERE em2.portfolio.id = em.portfolio.id" +
           ") " +
           "ORDER BY em.governanceScore DESC")
    List<EsgMetric> findGovernanceFocusedPortfolios(@Param("minScore") BigDecimal minScore);

    /**
     * Get ESG score change over time for a portfolio.
     */
    @Query("SELECT em.metricDate, em.overallEsgScore, " +
           "LAG(em.overallEsgScore) OVER (ORDER BY em.metricDate) as previousScore " +
           "FROM EsgMetric em " +
           "WHERE em.portfolio.id = :portfolioId " +
           "ORDER BY em.metricDate ASC")
    List<Object[]> getEsgScoreChanges(@Param("portfolioId") Long portfolioId);

    /**
     * Find portfolios with improving ESG scores.
     */
    @Query("SELECT em.portfolio.id, em.portfolio.portfolioName, " +
           "em.overallEsgScore as currentScore, " +
           "prev.em.overallEsgScore as previousScore " +
           "FROM EsgMetric em " +
           "JOIN EsgMetric prev ON em.portfolio.id = prev.portfolio.id " +
           "WHERE em.metricDate = :currentDate " +
           "AND prev.metricDate = :previousDate " +
           "AND em.overallEsgScore > prev.overallEsgScore " +
           "ORDER BY (em.overallEsgScore - prev.overallEsgScore) DESC")
    List<Object[]> findImprovingPortfolios(@Param("currentDate") LocalDate currentDate,
                                          @Param("previousDate") LocalDate previousDate);
}
