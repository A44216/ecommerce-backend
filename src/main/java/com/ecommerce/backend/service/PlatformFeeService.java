package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.responses.PlatformFeeResponse;
import com.ecommerce.backend.entity.PlatformFee;
import com.ecommerce.backend.repository.PlatformFeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class PlatformFeeService {

    private final PlatformFeeRepository platformFeeRepository;

    public PlatformFeeService(PlatformFeeRepository platformFeeRepository) {
        this.platformFeeRepository = platformFeeRepository;
    }

    @Transactional
    public PlatformFeeResponse getCurrentFee() {
        PlatformFee currentFee = platformFeeRepository.findByIsActiveTrue()
                .orElseGet(() -> {
                    PlatformFee defaultFee = new PlatformFee();
                    defaultFee.setRate(BigDecimal.ZERO);
                    defaultFee.setIsActive(true);
                    return platformFeeRepository.save(defaultFee);
                });

        return mapToResponse(currentFee);
    }

    private PlatformFeeResponse mapToResponse(PlatformFee fee) {
        return PlatformFeeResponse.builder()
                .id(fee.getId())
                .rate(fee.getRate())
                .isActive(fee.getIsActive())
                .build();
    }
}
