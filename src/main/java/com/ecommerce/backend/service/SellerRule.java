package com.ecommerce.backend.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SellerRule extends FuzzyRule {
    private String recommendation;

    public SellerRule(String code, String reason, String recommendation, double centroidValue, double firingStrength) {
        super(code, reason, centroidValue, firingStrength);
        this.recommendation = recommendation;
    }
}
