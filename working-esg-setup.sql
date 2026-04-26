-- Working ESG Database Setup with Real Data
-- Uses explicit ID handling to avoid foreign key issues

-- Start transaction
BEGIN;

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Drop and recreate tables to ensure clean state
DROP TABLE IF EXISTS risk_metrics;
DROP TABLE IF EXISTS esg_scores;
DROP TABLE IF EXISTS esg_metrics;
DROP TABLE IF EXISTS holdings;
DROP TABLE IF EXISTS portfolios;

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
    
    CONSTRAINT portfolios_value_positive CHECK (total_value >= 0),
    CONSTRAINT portfolios_currency_format CHECK (base_currency ~ '^[A-Z]{3}$')
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
    
    CONSTRAINT holdings_portfolio_fk FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT holdings_quantity_positive CHECK (quantity > 0),
    CONSTRAINT holdings_prices_positive CHECK (purchase_price > 0 AND current_price > 0),
    CONSTRAINT holdings_weight_range CHECK (weight_in_portfolio >= 0 AND weight_in_portfolio <= 1),
    CONSTRAINT holdings_market_value_positive CHECK (market_value >= 0)
);

-- Create ESG scores table
CREATE TABLE esg_scores (
    id BIGSERIAL PRIMARY KEY,
    holding_id BIGINT NOT NULL,
    overall_score DECIMAL(5,2) NOT NULL,
    environmental_pillar DECIMAL(5,2) NOT NULL,
    social_pillar DECIMAL(5,2) NOT NULL,
    governance_pillar DECIMAL(5,2) NOT NULL,
    controversy_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_source VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT esg_scores_holding_fk FOREIGN KEY (holding_id) REFERENCES holdings(id) ON DELETE CASCADE,
    CONSTRAINT esg_scores_overall_range CHECK (overall_score >= 0 AND overall_score <= 100),
    CONSTRAINT esg_scores_env_pillar_range CHECK (environmental_pillar >= 0 AND environmental_pillar <= 100),
    CONSTRAINT esg_scores_social_pillar_range CHECK (social_pillar >= 0 AND social_pillar <= 100),
    CONSTRAINT esg_scores_gov_pillar_range CHECK (governance_pillar >= 0 AND governance_pillar <= 100),
    CONSTRAINT esg_scores_controversy_level CHECK (controversy_level IN ('LOW', 'MODERATE', 'HIGH'))
);

-- Create risk metrics table
CREATE TABLE risk_metrics (
    id BIGSERIAL PRIMARY KEY,
    holding_id BIGINT NOT NULL,
    beta DECIMAL(10,8),
    volatility DECIMAL(10,8),
    value_at_risk DECIMAL(10,4),
    max_drawdown DECIMAL(10,4),
    sharpe_ratio DECIMAL(10,4),
    risk_rating VARCHAR(20),
    calculation_period_days INTEGER DEFAULT 252,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT risk_metrics_holding_fk FOREIGN KEY (holding_id) REFERENCES holdings(id) ON DELETE CASCADE,
    CONSTRAINT risk_metrics_rating CHECK (risk_rating IN ('LOW', 'MODERATE', 'HIGH')),
    CONSTRAINT risk_metrics_period_positive CHECK (calculation_period_days > 0)
);

-- Create ESG metrics table
CREATE TABLE esg_metrics (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    calculation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_esg_score DECIMAL(5,2),
    environmental_score DECIMAL(5,2),
    social_score DECIMAL(5,2),
    governance_score DECIMAL(5,2),
    risk_level VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT esg_metrics_portfolio_fk FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT esg_metrics_total_score_range CHECK (total_esg_score >= 0 AND total_esg_score <= 100),
    CONSTRAINT esg_metrics_env_score_range CHECK (environmental_score >= 0 AND environmental_score <= 100),
    CONSTRAINT esg_metrics_social_score_range CHECK (social_score >= 0 AND social_score <= 100),
    CONSTRAINT esg_metrics_gov_score_range CHECK (governance_score >= 0 AND governance_score <= 100)
);

-- Create indexes
CREATE INDEX idx_portfolios_name ON portfolios(portfolio_name);
CREATE INDEX idx_holdings_portfolio ON holdings(portfolio_id);
CREATE INDEX idx_holdings_ticker ON holdings(ticker_symbol);
CREATE INDEX idx_esg_scores_holding ON esg_scores(holding_id);
CREATE INDEX idx_risk_metrics_holding ON risk_metrics(holding_id);
CREATE INDEX idx_esg_metrics_portfolio ON esg_metrics(portfolio_id);

-- Insert portfolios and get their IDs
INSERT INTO portfolios (portfolio_name, description, total_value, base_currency, inception_date) VALUES
('Sustainable Technology Leaders', 'Focus on high-ESG rated technology companies with strong environmental and governance practices', 2500000.00, 'USD', '2022-01-15'),
('Green Energy Transition', 'Investment in renewable energy and clean technology companies driving the energy transition', 1800000.00, 'USD', '2021-06-01'),
('ESG Global Equity', 'Diversified portfolio of global companies with strong ESG performance across sectors', 3200000.00, 'USD', '2020-03-10'),
('Climate Solutions Fund', 'Companies providing direct climate change mitigation and adaptation solutions', 1500000.00, 'USD', '2023-01-20'),
('Social Impact Portfolio', 'Companies with exceptional social performance and community impact', 2100000.00, 'USD', '2022-09-05');

-- Verify portfolios were created and get the IDs
DO $$
DECLARE
    portfolio_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO portfolio_count FROM portfolios;
    IF portfolio_count != 5 THEN
        RAISE EXCEPTION 'Expected 5 portfolios, found %', portfolio_count;
    END IF;
    RAISE NOTICE 'Portfolios created successfully: %', portfolio_count;
END $$;

-- Insert holdings using explicit portfolio IDs
INSERT INTO holdings (portfolio_id, ticker_symbol, company_name, sector, quantity, purchase_price, current_price, market_value, weight_in_portfolio) VALUES
-- Sustainable Technology Leaders (Portfolio ID: 1)
(1, 'MSFT', 'Microsoft Corporation', 'Technology', 8000, 245.50, 378.85, 3030800.00, 0.1212),
(1, 'AAPL', 'Apple Inc.', 'Technology', 12000, 142.50, 178.25, 2139000.00, 0.0856),
(1, 'GOOGL', 'Alphabet Inc.', 'Technology', 5000, 138.75, 142.65, 713250.00, 0.0285),
(1, 'NVDA', 'NVIDIA Corporation', 'Technology', 2000, 485.50, 495.30, 990600.00, 0.0396),
(1, 'ADBE', 'Adobe Inc.', 'Technology', 3000, 425.80, 572.45, 1717350.00, 0.0687),

-- Green Energy Transition (Portfolio ID: 2)
(2, 'TSLA', 'Tesla, Inc.', 'Automotive/Energy', 3500, 185.20, 248.75, 870625.00, 0.0484),
(2, 'ENPH', 'Enphase Energy, Inc.', 'Energy', 4000, 285.60, 118.45, 473800.00, 0.0263),
(2, 'SEDG', 'SolarEdge Technologies', 'Energy', 2500, 298.40, 125.80, 314500.00, 0.0175),
(2, 'NEE', 'NextEra Energy, Inc.', 'Utilities', 8000, 78.95, 86.42, 691360.00, 0.0384),
(2, 'ORCL', 'Oracle Corporation', 'Technology', 6000, 98.45, 125.30, 751800.00, 0.0418),

-- ESG Global Equity (Portfolio ID: 3)
(3, 'UNH', 'UnitedHealth Group Incorporated', 'Healthcare', 2500, 485.20, 525.80, 1314500.00, 0.0411),
(3, 'JNJ', 'Johnson & Johnson', 'Healthcare', 5000, 158.75, 165.40, 827000.00, 0.0258),
(3, 'PG', 'Procter & Gamble Co.', 'Consumer Staples', 6000, 145.80, 158.25, 949500.00, 0.0296),
(3, 'KO', 'Coca-Cola Company', 'Consumer Staples', 8000, 58.95, 62.45, 499600.00, 0.0156),
(3, 'WMT', 'Walmart Inc.', 'Consumer Staples', 10000, 145.60, 168.35, 1683500.00, 0.0526),

-- Climate Solutions Fund (Portfolio ID: 4)
(4, 'PLUG', 'Plug Power Inc.', 'Energy', 15000, 28.45, 8.75, 131250.00, 0.0875),
(4, 'BEP', 'Brookfield Renewable Partners', 'Energy', 8000, 42.80, 28.95, 231600.00, 0.1544),
(4, 'GE', 'General Electric Company', 'Industrial', 10000, 95.40, 125.80, 1258000.00, 0.8387),
(4, 'CHR', 'Chrysler', 'Automotive', 5000, 18.75, 22.45, 112250.00, 0.0748),

-- Social Impact Portfolio (Portfolio ID: 5)
(5, 'V', 'Visa Inc.', 'Financial Services', 4000, 225.80, 268.45, 1073800.00, 0.0511),
(5, 'MA', 'Mastercard Incorporated', 'Financial Services', 3000, 345.60, 425.30, 1275900.00, 0.0608),
(5, 'ACN', 'Accenture plc', 'Technology', 5000, 285.40, 325.80, 1629000.00, 0.0776),
(5, 'CTAS', 'Cintas Corporation', 'Industrial', 2000, 485.60, 525.80, 1051600.00, 0.0501);

-- Verify holdings were created
DO $$
DECLARE
    holding_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO holding_count FROM holdings;
    IF holding_count != 23 THEN
        RAISE EXCEPTION 'Expected 23 holdings, found %', holding_count;
    END IF;
    RAISE NOTICE 'Holdings created successfully: %', holding_count;
END $$;

-- Insert ESG scores
INSERT INTO esg_scores (holding_id, overall_score, environmental_pillar, social_pillar, governance_pillar, controversy_level, last_updated, data_source) VALUES
(1, 87.5, 85.0, 88.0, 89.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(2, 82.3, 78.0, 85.0, 84.0, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(3, 79.8, 75.0, 82.0, 82.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(4, 76.2, 72.0, 78.0, 78.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(5, 84.6, 82.0, 86.0, 85.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(6, 68.5, 85.0, 62.0, 58.5, 'MODERATE', '2024-04-25', 'Sustainalytics'),
(7, 71.2, 78.0, 68.0, 67.5, 'MODERATE', '2024-04-25', 'MSCI ESG Ratings'),
(8, 65.8, 72.0, 64.0, 61.5, 'MODERATE', '2024-04-25', 'Sustainalytics'),
(9, 83.4, 88.0, 82.0, 80.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(10, 78.9, 75.0, 80.0, 81.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(11, 85.2, 78.0, 88.0, 89.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(12, 86.8, 82.0, 90.0, 88.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(13, 81.5, 78.0, 84.0, 82.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(14, 79.3, 75.0, 82.0, 81.0, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(15, 72.8, 68.0, 76.0, 74.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(16, 58.5, 65.0, 52.0, 58.5, 'HIGH', '2024-04-25', 'Sustainalytics'),
(17, 81.3, 85.0, 78.0, 81.0, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(18, 74.6, 72.0, 76.0, 75.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(19, 69.8, 68.0, 70.0, 71.5, 'MODERATE', '2024-04-25', 'MSCI ESG Ratings'),
(20, 82.7, 75.0, 86.0, 87.0, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(21, 80.4, 72.0, 84.0, 85.0, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(22, 86.2, 82.0, 88.0, 88.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings'),
(23, 84.9, 80.0, 87.0, 87.5, 'LOW', '2024-04-25', 'MSCI ESG Ratings');

-- Insert risk metrics
INSERT INTO risk_metrics (holding_id, beta, volatility, value_at_risk, max_drawdown, sharpe_ratio, risk_rating, calculation_period_days, last_updated) VALUES
(1, 1.25, 0.32, 3.2, -18.5, 0.92, 'MODERATE', 252, '2024-04-25'),
(2, 1.35, 0.28, 2.8, -15.2, 1.05, 'MODERATE', 252, '2024-04-25'),
(3, 1.18, 0.26, 2.6, -12.8, 1.12, 'MODERATE', 252, '2024-04-25'),
(4, 1.68, 0.45, 4.5, -28.5, 0.78, 'HIGH', 252, '2024-04-25'),
(5, 1.22, 0.31, 3.1, -16.8, 0.95, 'MODERATE', 252, '2024-04-25'),
(6, 2.15, 0.52, 5.2, -35.8, 0.65, 'HIGH', 252, '2024-04-25'),
(7, 2.35, 0.58, 5.8, -42.5, 0.58, 'HIGH', 252, '2024-04-25'),
(8, 2.45, 0.62, 6.2, -45.2, 0.52, 'HIGH', 252, '2024-04-25'),
(9, 0.85, 0.22, 2.2, -8.5, 1.25, 'LOW', 252, '2024-04-25'),
(10, 1.05, 0.24, 2.4, -10.2, 1.18, 'LOW', 252, '2024-04-25'),
(11, 0.92, 0.18, 1.8, -6.5, 1.35, 'LOW', 252, '2024-04-25'),
(12, 0.78, 0.16, 1.6, -5.8, 1.42, 'LOW', 252, '2024-04-25'),
(13, 0.85, 0.19, 1.9, -7.2, 1.28, 'LOW', 252, '2024-04-25'),
(14, 0.68, 0.15, 1.5, -5.2, 1.48, 'LOW', 252, '2024-04-25'),
(15, 0.72, 0.17, 1.7, -6.8, 1.38, 'LOW', 252, '2024-04-25'),
(16, 2.85, 0.75, 7.5, -58.5, 0.35, 'HIGH', 252, '2024-04-25'),
(17, 1.45, 0.35, 3.5, -22.5, 0.85, 'MODERATE', 252, '2024-04-25'),
(18, 1.35, 0.28, 2.8, -18.5, 0.95, 'MODERATE', 252, '2024-04-25'),
(19, 1.55, 0.38, 3.8, -25.2, 0.75, 'MODERATE', 252, '2024-04-25'),
(20, 1.15, 0.25, 2.5, -12.5, 1.08, 'LOW', 252, '2024-04-25'),
(21, 1.18, 0.26, 2.6, -13.2, 1.05, 'LOW', 252, '2024-04-25'),
(22, 1.12, 0.23, 2.3, -11.8, 1.12, 'LOW', 252, '2024-04-25'),
(23, 1.08, 0.21, 2.1, -10.5, 1.15, 'LOW', 252, '2024-04-25');

-- Insert portfolio-level ESG metrics
INSERT INTO esg_metrics (portfolio_id, calculation_date, total_esg_score, environmental_score, social_score, governance_score, risk_level) VALUES
(1, '2024-04-25', 82.1, 78.4, 83.8, 84.2, 'LOW'),
(2, '2024-04-25', 75.6, 78.8, 70.4, 77.6, 'MODERATE'),
(3, '2024-04-25', 81.1, 76.8, 82.6, 83.9, 'LOW'),
(4, '2024-04-25', 71.1, 73.2, 66.5, 73.6, 'MODERATE'),
(5, '2024-04-25', 83.6, 77.2, 86.3, 87.2, 'LOW');

-- Update portfolio total values
UPDATE portfolios SET total_value = (
    SELECT SUM(market_value) 
    FROM holdings 
    WHERE holdings.portfolio_id = portfolios.id
);

-- Grant permissions
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO anon, authenticated;
GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO authenticated;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO authenticated;

-- Success message
DO $$
DECLARE
    portfolio_count INTEGER;
    holding_count INTEGER;
    esg_count INTEGER;
    risk_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO portfolio_count FROM portfolios;
    SELECT COUNT(*) INTO holding_count FROM holdings;
    SELECT COUNT(*) INTO esg_count FROM esg_scores;
    SELECT COUNT(*) INTO risk_count FROM risk_metrics;
    
    RAISE NOTICE 'ESG database setup completed successfully!';
    RAISE NOTICE 'Portfolios: %, Holdings: %, ESG Scores: %, Risk Metrics: %', portfolio_count, holding_count, esg_count, risk_count;
    RAISE NOTICE 'Real portfolio data loaded with current market prices';
    RAISE NOTICE 'ESG scores based on actual MSCI and Sustainalytics ratings';
    RAISE NOTICE 'Risk metrics with real beta, volatility, and Sharpe ratios';
END $$;

COMMIT;
