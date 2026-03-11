-- Flyway migration: V1__init.sql
-- Creates initial schema for the quotation table inferred from JPA entity in the quotation module.

CREATE TABLE IF NOT EXISTS quotation (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP WITH TIME ZONE,
    currency_price NUMERIC(19,4),
    pct_change VARCHAR(50),
    pair VARCHAR(50)
);

-- Optional: create an index on pair if queries will filter by this column frequently
CREATE INDEX IF NOT EXISTS idx_quotation_pair ON quotation(pair);

