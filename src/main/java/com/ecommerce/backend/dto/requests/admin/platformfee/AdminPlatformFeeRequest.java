package com.ecommerce.backend.dto.requests.admin.platformfee;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminPlatformFeeRequest {

    @NotNull(message = "Tỷ lệ phí không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tỷ lệ phí phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Tỷ lệ phí không được vượt quá 100")
    private BigDecimal rate;
}
