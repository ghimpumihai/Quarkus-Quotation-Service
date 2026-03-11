-- Flyway migration: V1__init.sql
-- Creates initial schema for opportunity and quotation tables inferred from JPA entities.

CREATE TABLE IF NOT EXISTS opportunity (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP WITH TIME ZONE,
    proposal_id BIGINT,
    customer VARCHAR(255),
    price_tonne NUMERIC(19,4),
    last_currency_quotation NUMERIC(19,4)
);

CREATE TABLE IF NOT EXISTS quotation (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP WITH TIME ZONE,
    currency_price NUMERIC(19,4),
    pct_change VARCHAR(50),
    pair VARCHAR(50)
);

