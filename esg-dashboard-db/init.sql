-- ESG Portfolio Analytics Database Schema
-- PostgreSQL 15 compatible

-- Drop tables if they exist (for clean initialization)
DROP TABLE IF EXISTS risk_metrics CASCADE;
DROP TABLE IF EXISTS esg_scores CASCADE;
DROP TABLE IF EXISTS holdings CASCADE;
DROP TABLE IF EXISTS esg_metrics CASCADE;
DROP TABLE IF EXISTS portfolios CASCADE;

-- Create portfolios table
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    portfolio_name VARCHAR(255) NOT NULL,
    description TEXT,
    total_value DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    base_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    inception_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_portfolio_value_positive CHECK (total_value >= 0),
    CONSTRAINT chk_currency_format CHECK (base_currency ~ '^[A-Z]{3}$')
);

-- Create holdings table
CREATE TABLE holdings (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    ticker_symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    sector VARCHAR(100) NOT NULL,
    quantity DECIMAL(20,8) NOT NULL,
    purchase_price DECIMAL(20,4) NOT NULL,
    current_price DECIMAL(20,4) NOT NULL,
    market_value DECIMAL(20,2) NOT NULL,
    weight_in_portfolio DECIMAL(5,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_holdings_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_prices_positive CHECK (purchase_price > 0 AND current_price > 0),
    CONSTRAINT chk_weight_range CHECK (weight_in_portfolio >= 0 AND weight_in_portfolio <= 1),
    CONSTRAINT chk_market_value_positive CHECK (market_value >= 0)
);

-- Create ESG metrics table (portfolio-level ESG data)
CREATE TABLE esg_metrics (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    metric_date DATE NOT NULL,
    environmental_score DECIMAL(5,2) NOT NULL,
    social_score DECIMAL(5,2) NOT NULL,
    governance_score DECIMAL(5,2) NOT NULL,
    overall_esg_score DECIMAL(5,2) NOT NULL,
    controversy_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_esg_metrics_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT chk_esg_scores_range CHECK (
        environmental_score >= 0 AND environmental_score <= 100 AND
        social_score >= 0 AND social_score <= 100 AND
        governance_score >= 0 AND governance_score <= 100 AND
        overall_esg_score >= 0 AND overall_esg_score <= 100
    ),
    CONSTRAINT chk_controversy_non_negative CHECK (controversy_count >= 0),
    CONSTRAINT uq_portfolio_metric_date UNIQUE (portfolio_id, metric_date)
);

-- Create ESG scores table (company-level ESG ratings)
CREATE TABLE esg_scores (
    id BIGSERIAL PRIMARY KEY,
    holding_id BIGINT NOT NULL,
    score_date DATE NOT NULL,
    overall_score DECIMAL(5,2) NOT NULL,
    environmental_pillar DECIMAL(5,2) NOT NULL,
    social_pillar DECIMAL(5,2) NOT NULL,
    governance_pillar DECIMAL(5,2) NOT NULL,
    controversy_level VARCHAR(20) NOT NULL,
    data_provider VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_esg_scores_holding FOREIGN KEY (holding_id) REFERENCES holdings(id) ON DELETE CASCADE,
    CONSTRAINT chk_esg_pillars_range CHECK (
        overall_score >= 0 AND overall_score <= 100 AND
        environmental_pillar >= 0 AND environmental_pillar <= 100 AND
        social_pillar >= 0 AND social_pillar <= 100 AND
        governance_pillar >= 0 AND governance_pillar <= 100
    ),
    CONSTRAINT chk_controversy_level CHECK (controversy_level IN ('LOW', 'MODERATE', 'HIGH', 'VERY_HIGH')),
    CONSTRAINT uq_holding_score_date UNIQUE (holding_id, score_date)
);

-- Create risk metrics table
CREATE TABLE risk_metrics (
    id BIGSERIAL PRIMARY KEY,
    holding_id BIGINT NOT NULL,
    calculation_date DATE NOT NULL,
    beta DECIMAL(8,4),
    volatility DECIMAL(8,4),
    value_at_risk DECIMAL(8,4),
    max_drawdown DECIMAL(8,4),
    sharpe_ratio DECIMAL(8,4),
    risk_rating VARCHAR(20) NOT NULL,
    time_horizon_days INTEGER NOT NULL DEFAULT 252, -- Trading days in a year
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_risk_metrics_holding FOREIGN KEY (holding_id) REFERENCES holdings(id) ON DELETE CASCADE,
    CONSTRAINT chk_risk_ranges CHECK (
        (beta IS NULL OR beta >= -10 AND beta <= 10) AND
        (volatility IS NULL OR volatility >= 0 AND volatility <= 10) AND
        (value_at_risk IS NULL OR value_at_risk >= -100 AND value_at_risk <= 0) AND
        (max_drawdown IS NULL OR max_drawdown >= -100 AND max_drawdown <= 0) AND
        (sharpe_ratio IS NULL OR sharpe_ratio >= -10 AND sharpe_ratio <= 10)
    ),
    CONSTRAINT chk_risk_rating CHECK (risk_rating IN ('LOW', 'MODERATE', 'HIGH', 'VERY_HIGH')),
    CONSTRAINT chk_time_horizon_positive CHECK (time_horizon_days > 0),
    CONSTRAINT uq_holding_risk_date UNIQUE (holding_id, calculation_date)
);

-- Create indexes for performance optimization
CREATE INDEX idx_portfolios_name ON portfolios(portfolio_name);
CREATE INDEX idx_portfolios_created ON portfolios(created_at);

CREATE INDEX idx_holdings_portfolio ON holdings(portfolio_id);
CREATE INDEX idx_holdings_ticker ON holdings(ticker_symbol);
CREATE INDEX idx_holdings_sector ON holdings(sector);

CREATE INDEX idx_esg_metrics_portfolio ON esg_metrics(portfolio_id);
CREATE INDEX idx_esg_metrics_date ON esg_metrics(metric_date);

CREATE INDEX idx_esg_scores_holding ON esg_scores(holding_id);
CREATE INDEX idx_esg_scores_date ON esg_scores(score_date);
CREATE INDEX idx_esg_scores_overall ON esg_scores(overall_score);

CREATE INDEX idx_risk_metrics_holding ON risk_metrics(holding_id);
CREATE INDEX idx_risk_metrics_date ON risk_metrics(calculation_date);
CREATE INDEX idx_risk_metrics_rating ON risk_metrics(risk_rating);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_portfolios_updated_at 
    BEFORE UPDATE ON portfolios 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_holdings_updated_at 
    BEFORE UPDATE ON holdings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for demonstration
INSERT INTO portfolios (portfolio_name, description, total_value, base_currency, inception_date) VALUES
('Tech Innovation Fund', 'Focused on technology companies with strong ESG profiles', 5000000.00, 'USD', '2023-01-15'),
('Sustainable Energy Portfolio', 'Renewable energy and clean technology investments', 2500000.00, 'USD', '2023-03-20'),
('Global ESG Leaders', 'Diversified portfolio of global ESG leaders', 10000000.00, 'USD', '2022-11-10');

-- Sample holdings for Tech Innovation Fund
INSERT INTO holdings (portfolio_id, ticker_symbol, company_name, sector, quantity, purchase_price, current_price, market_value, weight_in_portfolio) VALUES
(1, 'MSFT', 'Microsoft Corporation', 'Technology', 10000, 350.00, 380.25, 3802500.00, 0.7605),
(1, 'AAPL', 'Apple Inc.', 'Technology', 5000, 150.00, 175.50, 877500.00, 0.1755),
(1, 'GOOGL', 'Alphabet Inc.', 'Technology', 2000, 120.00, 140.75, 281500.00, 0.0563),
(1, 'TSLA', 'Tesla Inc.', 'Automotive', 500, 200.00, 180.25, 90125.00, 0.0180);

-- Sample holdings for Sustainable Energy Portfolio
INSERT INTO holdings (portfolio_id, ticker_symbol, company_name, sector, quantity, purchase_price, current_price, market_value, weight_in_portfolio) VALUES
(2, 'NEE', 'NextEra Energy Inc.', 'Utilities', 8000, 75.00, 85.50, 684000.00, 0.2736),
(2, 'SHEL', 'Shell plc', 'Energy', 5000, 60.00, 65.25, 326250.00, 0.1305),
(2, 'ENPH', 'Enphase Energy Inc.', 'Technology', 3000, 180.00, 195.75, 587250.00, 0.2349),
(2, 'PLUG', 'Plug Power Inc.', 'Energy', 10000, 15.00, 12.50, 125000.00, 0.0500),
(2, 'FSLR', 'First Solar Inc.', 'Technology', 4000, 200.00, 225.50, 902000.00, 0.3608);

-- Sample ESG scores for holdings
INSERT INTO esg_scores (holding_id, score_date, overall_score, environmental_pillar, social_pillar, governance_pillar, controversy_level, data_provider) VALUES
(1, '2024-04-01', 78.5, 75.0, 80.0, 80.0, 'LOW', 'MSCI'),
(2, '2024-04-01', 82.3, 85.0, 80.0, 82.0, 'LOW', 'Sustainalytics'),
(3, '2024-04-01', 75.8, 72.0, 78.0, 77.0, 'MODERATE', 'MSCI'),
(4, '2024-04-01', 71.2, 68.0, 73.0, 73.0, 'HIGH', 'Sustainalytics'),
(5, '2024-04-01', 79.6, 82.0, 78.0, 79.0, 'LOW', 'MSCI'),
(6, '2024-04-01', 68.4, 65.0, 70.0, 70.0, 'HIGH', 'Sustainalytics');

-- Sample risk metrics
INSERT INTO risk_metrics (holding_id, calculation_date, beta, volatility, value_at_risk, max_drawdown, sharpe_ratio, risk_rating, time_horizon_days) VALUES
(1, '2024-04-01', 1.05, 0.28, -2.5, -15.5, 0.95, 'MODERATE', 252),
(2, '2024-04-01', 1.15, 0.32, -2.8, -18.2, 0.88, 'MODERATE', 252),
(3, '2024-04-01', 1.08, 0.30, -2.6, -16.8, 0.92, 'MODERATE', 252),
(4, '2024-04-01', 1.85, 0.45, -4.2, -25.3, 0.65, 'HIGH', 252),
(5, '2024-04-01', 0.75, 0.22, -2.0, -12.5, 1.05, 'LOW', 252),
(6, '2024-04-01', 1.25, 0.35, -3.0, -20.1, 0.78, 'MODERATE', 252);

-- Sample portfolio ESG metrics
INSERT INTO esg_metrics (portfolio_id, metric_date, environmental_score, social_score, governance_score, overall_esg_score, controversy_count) VALUES
(1, '2024-04-01', 75.0, 77.8, 78.0, 76.9, 2),
(2, '2024-04-01', 73.5, 74.0, 74.5, 74.0, 3);

-- Create view for portfolio summaries
CREATE OR REPLACE VIEW portfolio_summaries AS
SELECT 
    p.id,
    p.portfolio_name,
    p.total_value,
    p.base_currency,
    p.inception_date,
    COUNT(h.id) as holding_count,
    COALESCE(AVG(es.overall_score), 0) as average_esg_score,
    COALESCE(MAX(rm.risk_rating), 'UNKNOWN') as top_risk_level,
    COALESCE(AVG(rm.beta), 0) as average_beta,
    COALESCE(AVG(rm.volatility), 0) as average_volatility,
    p.created_at,
    p.updated_at
FROM portfolios p
LEFT JOIN holdings h ON p.id = h.portfolio_id
LEFT JOIN esg_scores es ON h.id = es.holding_id AND es.score_date = (
    SELECT MAX(score_date) FROM esg_scores es2 WHERE es2.holding_id = h.id
)
LEFT JOIN risk_metrics rm ON h.id = rm.holding_id AND rm.calculation_date = (
    SELECT MAX(calculation_date) FROM risk_metrics rm2 WHERE rm2.holding_id = h.id
)
GROUP BY p.id, p.portfolio_name, p.total_value, p.base_currency, p.inception_date, p.created_at, p.updated_at;

-- Grant permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO esg_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO esg_user;

-- Create user for the application (commented out - uncomment for production)
-- DO $$
-- BEGIN
--     IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'esg_user') THEN
--         CREATE ROLE esg_user LOGIN PASSWORD 'esg_password';
--     END IF;
-- END
-- $$;

COMMIT;
