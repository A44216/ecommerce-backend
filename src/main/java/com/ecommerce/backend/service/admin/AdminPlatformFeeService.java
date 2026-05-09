package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.requests.admin.platformfee.AdminPlatformFeeRequest;
import com.ecommerce.backend.dto.responses.admin.platformfee.AdminPlatformFeeResponse;
import com.ecommerce.backend.entity.PlatformFee;
import com.ecommerce.backend.repository.PlatformFeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class AdminPlatformFeeService {

    private final PlatformFeeRepository platformFeeRepository;

    public AdminPlatformFeeService(PlatformFeeRepository platformFeeRepository) {
        this.platformFeeRepository = platformFeeRepository;
    }

    public AdminPlatformFeeResponse getCurrentFee() {
        PlatformFee currentFee = platformFeeRepository.findByIsActiveTrue()
                .orElseGet(() -> {
                    PlatformFee defaultFee = new PlatformFee();
                    defaultFee.setRate(BigDecimal.ZERO);
                    defaultFee.setIsActive(true);
                    return platformFeeRepository.save(defaultFee);
                });

        return mapToResponse(currentFee);
    }

    @Transactional
    public AdminPlatformFeeResponse updateCurrentFee(AdminPlatformFeeRequest request) {
        PlatformFee currentFee = platformFeeRepository.findByIsActiveTrue()
                .orElseGet(() -> {
                    PlatformFee newFee = new PlatformFee();
                    newFee.setIsActive(true);
                    return newFee;
                });

        currentFee.setRate(request.getRate());
        PlatformFee savedFee = platformFeeRepository.save(currentFee);

        return mapToResponse(savedFee);
    }

    private AdminPlatformFeeResponse mapToResponse(PlatformFee fee) {
        return AdminPlatformFeeResponse.builder()
                .id(fee.getId())
                .rate(fee.getRate())
                .isActive(fee.getIsActive())
                .build();
    }
}
