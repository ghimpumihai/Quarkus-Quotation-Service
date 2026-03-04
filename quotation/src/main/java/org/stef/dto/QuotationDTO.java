package org.stef.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;


@Builder
public record QuotationDTO(Date date, BigDecimal currencyPrice) { }
