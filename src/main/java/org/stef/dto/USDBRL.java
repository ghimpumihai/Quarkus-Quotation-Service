package org.stef.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;


@Builder
public record USDBRL(
        String code,
        String codein,
        String name,
        String high,
        String low,
        String varBid,
        String pctChange,
        String bid,
        String ask,
        String timestamp,
        String create_date) {}
