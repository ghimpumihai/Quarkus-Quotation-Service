-- Flyway migration: V1__init.sql
-- Creates initial schema for the proposal table inferred from JPA entity in the proposal module.

CREATE TABLE IF NOT EXISTS proposal (
    id BIGSERIAL PRIMARY KEY,
    customer VARCHAR(255) NOT NULL,
    price_tonne NUMERIC(19,4),
    tonnes INTEGER,
    country VARCHAR(100),
    proposal_validity_days INTEGER,
    created TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Optional index to speed lookup by customer
CREATE INDEX IF NOT EXISTS idx_proposal_customer ON proposal(customer);

