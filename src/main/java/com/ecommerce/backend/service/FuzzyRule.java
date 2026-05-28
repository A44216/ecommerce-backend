package com.ecommerce.backend.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuzzyRule {
    private String code;
    private String reason;
    private double centroidValue; // Điểm trọng tâm của tập mờ đầu ra (ci)
    private double firingStrength; // Mức độ kích hoạt của luật (α)
}
