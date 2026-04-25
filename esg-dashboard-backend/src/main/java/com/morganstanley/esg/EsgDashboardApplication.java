package com.morganstanley.esg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ESG Portfolio Dashboard Application
 * 
 * Main Spring Boot application class for the ESG Portfolio Analytics & Risk Dashboard.
 * This application provides REST APIs for portfolio management, ESG scoring, and risk analysis.
 * 
 * @author Morgan Stanley Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class EsgDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsgDashboardApplication.class, args);
    }
}
