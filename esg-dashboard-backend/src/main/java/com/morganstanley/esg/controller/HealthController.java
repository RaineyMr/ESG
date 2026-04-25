package com.morganstanley.esg.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller.
 * 
 * Provides health check endpoints for monitoring the application status,
 * database connectivity, and overall system health.
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "APIs for monitoring application health")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    /**
     * Basic health check endpoint.
     */
    @Operation(summary = "Basic health check", description = "Verifies that the application is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        logger.debug("GET /health - Basic health check");
        return ResponseEntity.ok("ESG Portfolio Dashboard is healthy");
    }

    /**
     * Detailed health check with database connectivity.
     */
    @Operation(summary = "Detailed health check", description = "Provides detailed health status including database connectivity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All systems healthy"),
        @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        logger.debug("GET /health/detailed - Detailed health check");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "ESG Portfolio Dashboard");
        health.put("version", "1.0.0");
        
        // Check database connectivity
        boolean databaseHealthy = checkDatabaseHealth();
        health.put("database", databaseHealthy ? "UP" : "DOWN");
        
        // Overall status
        boolean overallHealthy = databaseHealthy;
        health.put("overall", overallHealthy ? "UP" : "DOWN");
        
        logger.debug("Health check completed - Database: {}, Overall: {}", 
                    databaseHealthy ? "UP" : "DOWN", 
                    overallHealthy ? "UP" : "DOWN");
        
        return overallHealthy ? ResponseEntity.ok(health) : 
                              ResponseEntity.status(503).body(health);
    }

    /**
     * Database health check.
     */
    @Operation(summary = "Database health check", description = "Verifies database connectivity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database is healthy"),
        @ApiResponse(responseCode = "503", description = "Database is unavailable")
    })
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealthCheck() {
        logger.debug("GET /health/database - Database health check");
        
        Map<String, Object> dbHealth = new HashMap<>();
        dbHealth.put("timestamp", LocalDateTime.now());
        
        boolean isHealthy = checkDatabaseHealth();
        dbHealth.put("status", isHealthy ? "UP" : "DOWN");
        
        if (isHealthy) {
            dbHealth.put("message", "Database connection successful");
        } else {
            dbHealth.put("message", "Database connection failed");
            dbHealth.put("error", "Unable to establish connection to database");
        }
        
        logger.debug("Database health check completed: {}", isHealthy ? "UP" : "DOWN");
        
        return isHealthy ? ResponseEntity.ok(dbHealth) : 
                         ResponseEntity.status(503).body(dbHealth);
    }

    /**
     * Application info endpoint.
     */
    @Operation(summary = "Application information", description = "Provides application metadata and information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application information retrieved successfully")
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> applicationInfo() {
        logger.debug("GET /health/info - Application information");
        
        Map<String, Object> info = new HashMap<>();
        info.put("application", "ESG Portfolio Dashboard");
        info.put("version", "1.0.0");
        info.put("description", "Enterprise ESG Portfolio Analytics & Risk Dashboard");
        info.put("buildTime", "2024-04-25T18:00:00Z");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springBootVersion", "3.1.5");
        info.put("timestamp", LocalDateTime.now());
        
        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memory.put("maxMemory", runtime.maxMemory());
        info.put("memory", memory);
        
        logger.debug("Application information retrieved");
        return ResponseEntity.ok(info);
    }

    /**
     * Readiness probe endpoint.
     */
    @Operation(summary = "Readiness probe", description = "Kubernetes readiness probe endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is ready"),
        @ApiResponse(responseCode = "503", description = "Application is not ready")
    })
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readinessProbe() {
        logger.debug("GET /health/ready - Readiness probe");
        
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("timestamp", LocalDateTime.now());
        
        boolean isReady = checkDatabaseHealth();
        readiness.put("ready", isReady);
        
        if (isReady) {
            readiness.put("message", "Application is ready to serve traffic");
        } else {
            readiness.put("message", "Application is not ready - database unavailable");
        }
        
        logger.debug("Readiness probe completed: {}", isReady ? "READY" : "NOT_READY");
        
        return isReady ? ResponseEntity.ok(readiness) : 
                        ResponseEntity.status(503).body(readiness);
    }

    /**
     * Liveness probe endpoint.
     */
    @Operation(summary = "Liveness probe", description = "Kubernetes liveness probe endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is alive")
    })
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> livenessProbe() {
        logger.debug("GET /health/live - Liveness probe");
        
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("alive", true);
        liveness.put("timestamp", LocalDateTime.now());
        liveness.put("message", "Application is alive and responding");
        
        logger.debug("Liveness probe completed: ALIVE");
        return ResponseEntity.ok(liveness);
    }

    /**
     * Metrics endpoint.
     */
    @Operation(summary = "Application metrics", description = "Provides basic application metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    })
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        logger.debug("GET /health/metrics - Application metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now());
        
        // Memory metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        metrics.put("memory", memory);
        
        // Thread metrics
        Map<String, Object> threads = new HashMap<>();
        threads.put("active", Thread.activeCount());
        metrics.put("threads", threads);
        
        // System properties
        Map<String, Object> system = new HashMap<>();
        system.put("processors", runtime.availableProcessors());
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        metrics.put("system", system);
        
        logger.debug("Metrics retrieved");
        return ResponseEntity.ok(metrics);
    }

    /**
     * Private helper method to check database health.
     */
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            logger.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}
