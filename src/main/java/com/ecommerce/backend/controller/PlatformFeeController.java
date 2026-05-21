package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.responses.PlatformFeeResponse;
import com.ecommerce.backend.service.PlatformFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform-fees")
@CrossOrigin
public class PlatformFeeController {

    private final PlatformFeeService platformFeeService;

    public PlatformFeeController(PlatformFeeService platformFeeService) {
        this.platformFeeService = platformFeeService;
    }

    @GetMapping("/current")
    public ResponseEntity<PlatformFeeResponse> getCurrentFee() {
        return ResponseEntity.ok(platformFeeService.getCurrentFee());
    }
}
