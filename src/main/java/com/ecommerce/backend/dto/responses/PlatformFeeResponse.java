package com.ecommerce.backend.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlatformFeeResponse {
    private Integer id;
    private BigDecimal rate;
    private Boolean isActive;
}
