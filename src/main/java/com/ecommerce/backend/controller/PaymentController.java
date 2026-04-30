package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/create-payment")
    public ResponseEntity<Map<String, String>> createPayment(
            @RequestParam("amount") long amount,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {
        
        String paymentUrl = paymentService.createPaymentUrl(amount, orderInfo, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Successfully created payment url");
        response.put("url", paymentUrl);
        
        return ResponseEntity.ok(response);
    }
}
