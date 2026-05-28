package com.ecommerce.backend.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryContext {
    private int sMax;
    private BigDecimal pMin;
    private BigDecimal pMax;
}
