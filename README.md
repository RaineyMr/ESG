# ESG Portfolio Analytics & Risk Dashboard

A full-stack enterprise-grade platform for investment teams to analyze ESG metrics, portfolio insights, and risk metrics. Built with Spring Boot, Angular, PostgreSQL, and Docker.

## 🎯 Project Overview

This application demonstrates a complete full-stack implementation of a financial analytics platform featuring:

- **Backend**: Spring Boot REST API with comprehensive business logic
- **Frontend**: Angular dashboard with professional UI/UX
- **Database**: PostgreSQL with optimized schema and indexes
- **DevOps**: Docker & Kubernetes ready with docker-compose

### Key Features

✅ **Portfolio Management** - Create and manage investment portfolios
✅ **ESG Scoring** - Calculate and track Environmental, Social, and Governance metrics
✅ **Risk Analytics** - Comprehensive risk assessment (Beta, Volatility, Value at Risk, Sharpe Ratio)
✅ **Real-time Dashboards** - Interactive visualizations and metrics
✅ **Data Ingestion** - Ingest portfolio and market data
✅ **Drill-down Analysis** - Analyze individual holdings and sectors
✅ **Automated Testing** - CI/CD pipeline ready
✅ **Enterprise Scale** - Built for production use

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Angular Frontend (4200)               │
│              Professional Dashboard with Charts          │
└─────────────────┬───────────────────────────────────────┘
                  │ HTTP/REST
┌─────────────────▼───────────────────────────────────────┐
│              Nginx Reverse Proxy (80/443)               │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│         Spring Boot API (8080) - Rest Controllers       │
│              Service Layer - Business Logic             │
│           Repository Layer - Data Access               │
└─────────────────┬───────────────────────────────────────┘
                  │ JDBC
┌─────────────────▼───────────────────────────────────────┐
│      PostgreSQL (5432) - Relational Database            │
│   - Portfolios, Holdings, ESG Metrics, Risk Metrics    │
└─────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
esg-portfolio-dashboard/
├── esg-dashboard-backend/
│   ├── src/main/java/com/morganstanley/esg/
│   │   ├── model/                 # JPA Entity classes
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── service/               # Business logic
│   │   ├── controller/            # REST API endpoints
│   │   ├── dto/                   # Data Transfer Objects
│   │   └── EsgDashboardApplication.java
│   ├── src/main/resources/
│   │   └── application.yml        # Application configuration
│   ├── pom.xml                    # Maven dependencies
│   └── Dockerfile
├── esg-dashboard-frontend/
│   ├── src/app/
│   │   ├── services/              # Angular services
│   │   ├── models/                # TypeScript models
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   └── app.component.scss
│   ├── package.json
│   └── Dockerfile
├── esg-dashboard-db/
│   └── init.sql                   # Database schema & seed data
├── docker-compose.yml             # Container orchestration
├── nginx.conf                      # Reverse proxy config
└── README.md
```

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose (recommended)
- OR: Java 17+, Node.js 18+, PostgreSQL 15+

### Option 1: Docker Compose (Recommended)

```bash
# Clone or navigate to project directory
cd esg-portfolio-dashboard

# Start all services
docker-compose up -d

# Wait for services to be healthy (30-60 seconds)
docker-compose ps

# Access the application
# Frontend: http://localhost:4200
# Backend API: http://localhost:8080/api
# Nginx Proxy: http://localhost
```

### Option 2: Manual Setup

#### 1. Database Setup
```bash
# Start PostgreSQL
docker run -d \
  --name esg-postgres \
  -e POSTGRES_DB=esg_portfolio_db \
  -e POSTGRES_USER=esg_user \
  -e POSTGRES_PASSWORD=esg_password \
  -p 5432:5432 \
  postgres:15-alpine

# Initialize database
psql -h localhost -U esg_user -d esg_portfolio_db -f esg-dashboard-db/init.sql
```

#### 2. Backend Setup
```bash
cd esg-dashboard-backend

# Build
mvn clean package

# Run
java -jar target/esg-portfolio-dashboard-1.0.0.jar
```

#### 3. Frontend Setup
```bash
cd esg-dashboard-frontend

# Install dependencies
npm install

# Start dev server
npm start

# Or build for production
npm run build
```

## 🔌 API Endpoints

### Portfolio Management

#### Get All Portfolios
```
GET /api/portfolios
Response: Array<PortfolioDTO>
```

#### Get Portfolio by ID
```
GET /api/portfolios/{id}
Response: PortfolioDTO
```

#### Get Portfolio Summary
```
GET /api/portfolios/{id}/summary
Response: PortfolioSummaryDTO
{
  "portfolioId": 1,
  "portfolioName": "Tech Innovation Fund",
  "totalValue": 5000000,
  "averageESGScore": 72.5,
  "holdingCount": 3,
  "topRisk": 4.75,
  "overallRiskLevel": "HIGH"
}
```

#### Create Portfolio
```
POST /api/portfolios
Body: {
  "portfolioName": "My Portfolio",
  "totalValue": 1000000,
  "baseCurrency": "USD"
}
Response: PortfolioDTO
```

#### Add Holding
```
POST /api/portfolios/{id}/holdings
Body: {
  "tickerSymbol": "MSFT",
  "companyName": "Microsoft",
  "sector": "Technology",
  "quantity": 100,
  "currentPrice": 380.25,
  "marketValue": 38025
}
Response: HoldingDTO
```

### ESG & Risk Management

#### Update ESG Score
```
POST /api/holdings/{id}/esg-score
Body: {
  "overallScore": 78.5,
  "environmentalPillar": 75,
  "socialPillar": 80,
  "governancePillar": 80,
  "controversyLevel": "LOW"
}
Response: ESGScoreDTO
```

#### Add Risk Metric
```
POST /api/holdings/{id}/risk-metric
Body: {
  "beta": 1.05,
  "volatility": 0.28,
  "valueAtRisk": 2.5,
  "maxDrawdown": -15.5,
  "sharpeRatio": 0.95
}
Response: RiskMetricDTO
```

## 📊 Data Models

### Portfolio
```typescript
{
  id: number;
  portfolioName: string;
  description: string;
  totalValue: BigDecimal;
  baseCurrency: string;
  inceptionDate: LocalDateTime;
  holdings: Holding[];
  esgMetrics: ESGMetric[];
}
```

### Holding
```typescript
{
  id: number;
  tickerSymbol: string;
  companyName: string;
  sector: string;
  quantity: BigDecimal;
  currentPrice: BigDecimal;
  marketValue: BigDecimal;
  weightInPortfolio: BigDecimal;
  purchasePrice: BigDecimal;
  esgScore: ESGScore;
  riskMetrics: RiskMetric[];
}
```

### ESG Score (0-100)
```typescript
{
  overallScore: BigDecimal;      // Combined E+S+G
  environmentalPillar: BigDecimal; // Carbon, emissions, energy
  socialPillar: BigDecimal;        // Employees, community
  governancePillar: BigDecimal;   // Board, compensation, audit
  controversyLevel: string;       // LOW, MODERATE, HIGH
}
```

### Risk Metrics
```typescript
{
  beta: BigDecimal;              // Market sensitivity
  volatility: BigDecimal;         // Price volatility
  valueAtRisk: BigDecimal;        // 95% confidence VAR
  maxDrawdown: BigDecimal;        // Peak-to-trough decline
  sharpeRatio: BigDecimal;        // Risk-adjusted return
  riskRating: string;             // LOW, MODERATE, HIGH
}
```

## 🎨 Frontend Features

### Dashboard Components

- **Portfolio Selector** - Browse and select portfolios
- **ESG Score Card** - Visual representation of ESG rating
- **Risk Assessment** - Color-coded risk levels
- **Holdings Table** - Detailed security breakdown
- **Performance Metrics** - Real-time analytics
- **Responsive Design** - Works on desktop and tablet

### UI/UX Highlights

- Dark theme optimized for financial data viewing
- Smooth animations and transitions
- Real-time data updates
- Drill-down capabilities
- Professional typography and spacing
- Accessibility compliant

## 🔐 Security Considerations

### Authentication (TODO - Implement)
- Add Spring Security with JWT tokens
- Implement role-based access control (RBAC)
- Support OAuth 2.0 providers

### Data Protection
- Encrypt sensitive data at rest
- Use HTTPS/TLS in production
- Implement rate limiting
- Add CORS validation
- SQL injection prevention (using parameterized queries)

### Implementation
```java
// Add to pom.xml:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 📈 Performance Optimization

### Database
- Strategic indexing on frequently queried columns
- Connection pooling with HikariCP
- Lazy loading for relationships
- Pagination for large result sets

### Backend
- Response caching with Spring Cache
- Batch processing for bulk operations
- Asynchronous processing with @Async

### Frontend
- Lazy loading of modules
- OnPush change detection
- Angular CLI optimizations
- Minification and bundling

## 🧪 Testing

### Backend Tests
```bash
cd esg-dashboard-backend

# Run all tests
mvn test

# Run with coverage
mvn jacoco:report

# Specific test class
mvn test -Dtest=PortfolioServiceTest
```

### Frontend Tests
```bash
cd esg-dashboard-frontend

# Run tests
npm test

# Coverage report
npm test -- --code-coverage

# End-to-end testing
npm run e2e
```

## 📦 Build & Deployment

### Build Artifacts
```bash
# Backend JAR
mvn clean package
# Output: target/esg-portfolio-dashboard-1.0.0.jar

# Frontend Bundle
npm run build
# Output: dist/esg-portfolio-dashboard/
```

### Docker Build
```bash
# Build all images
docker-compose build

# Push to registry (example)
docker tag esg-dashboard-backend:latest myregistry/esg-backend:1.0.0
docker push myregistry/esg-backend:1.0.0
```

### Kubernetes Deployment
```bash
# Build Docker images first
docker-compose build

# Deploy to Kubernetes
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/postgres-deployment.yml
kubectl apply -f k8s/backend-deployment.yml
kubectl apply -f k8s/frontend-deployment.yml
```

## 🔄 CI/CD Pipeline

### GitHub Actions Example
```yaml
name: Build & Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Build Backend
        run: mvn clean package
      
      - name: Build Frontend
        run: npm ci && npm run build
      
      - name: Push Docker Images
        run: docker-compose build && docker push ...
      
      - name: Deploy to Production
        run: kubectl apply -f k8s/
```

## 📚 Tech Stack Details

### Backend
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **ORM**: JPA/Hibernate
- **Database**: PostgreSQL 15
- **Build Tool**: Maven
- **Logging**: SLF4J + Logback

### Frontend
- **Framework**: Angular 17
- **Language**: TypeScript 5.2
- **Styling**: SCSS
- **HTTP Client**: HttpClient
- **Build Tool**: Angular CLI

### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose / Kubernetes
- **Reverse Proxy**: Nginx
- **CI/CD**: GitHub Actions (optional)

## 🚨 Troubleshooting

### Backend won't start
```bash
# Check logs
docker-compose logs backend

# Verify database connection
docker-compose logs postgres

# Restart services
docker-compose restart backend
```

### Frontend blank page
```bash
# Clear browser cache
# Hard refresh: Ctrl+Shift+R (Windows) or Cmd+Shift+R (Mac)

# Check console for errors
# F12 > Console tab

# Verify backend connectivity
curl http://localhost:8080/api/health
```

### Database connection error
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check database exists
docker-compose exec postgres psql -U esg_user -d esg_portfolio_db -c "\dt"

# Reinitialize if needed
docker-compose down -v
docker-compose up -d
```

## 📖 API Documentation

Generate Swagger/OpenAPI docs:
```java
// Add to pom.xml:
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.0</version>
</dependency>

// Access at http://localhost:8080/swagger-ui.html
```

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/amazing-feature`
2. Commit changes: `git commit -m 'Add amazing feature'`
3. Push to branch: `git push origin feature/amazing-feature`
4. Open a Pull Request

## 📝 Code Style

### Java
- Follow Google Java Style Guide
- Use Lombok for boilerplate reduction
- Write comprehensive unit tests

### TypeScript/Angular
- Use strict mode
- Follow Angular style guide
- Document public APIs with JSDoc

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 📞 Support & Contact

For issues, questions, or contributions:
- GitHub Issues: [Project Issues]
- Email: engineering@morganstanley.com
- Slack: #esg-dashboard-team

## 🎓 Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [RESTful API Best Practices](https://restfulapi.net/)

---

**Version**: 1.0.0  
**Last Updated**: April 25, 2026  
**Maintainer**: Morgan Stanley Engineering Team
