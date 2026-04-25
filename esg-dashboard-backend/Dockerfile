# ESG Portfolio Dashboard Backend Dockerfile
# Multi-stage build for optimized production image

# Build Stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Production Stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create application user
RUN addgroup -g 1001 -S esguser && \
    adduser -S esguser -u 1001

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/esg-portfolio-dashboard-*.jar app.jar

# Change ownership to non-root user
RUN chown -R esguser:esguser /app

# Switch to non-root user
USER esguser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# JVM Options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
