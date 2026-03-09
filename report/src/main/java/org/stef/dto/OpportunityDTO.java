package org.stef.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Builder
@Jacksonized
public record OpportunityDTO (
        Long proposalId,
        String customer,
        BigDecimal priceTonne,
        BigDecimal lastCurrencyQuotation
) {}
