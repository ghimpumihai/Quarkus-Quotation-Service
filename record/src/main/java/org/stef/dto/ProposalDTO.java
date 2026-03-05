package org.stef.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Builder
@Jacksonized
public record ProposalDTO(
        Long proposalId,
        String customer,
        BigDecimal priceTonne
) {}
