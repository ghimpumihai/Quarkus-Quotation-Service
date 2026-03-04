package org.stef.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Builder
@Jacksonized
public record ProposalDetailsDTO(
        Long proposalId,
        String customer,
        BigDecimal priceTonne,
        Integer tonnes,
        String country,
        Integer proposalValidityDays
) {}
