package com.ecommerce.backend.dto.responses.admin.platformfee;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminPlatformFeeResponse {
    private Integer id;
    private BigDecimal rate;
    private Boolean isActive;
}
