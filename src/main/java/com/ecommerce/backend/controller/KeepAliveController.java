package com.ecommerce.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class KeepAliveController {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @GetMapping("/api/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Server is awake!");
    }

    @GetMapping("/api/test-models")
    public ResponseEntity<String> testModels() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/api/test-gemini")
    public ResponseEntity<String> testGemini() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> part = new HashMap<>();
            part.put("text", "Hello");
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, request, Map.class);
            return ResponseEntity.ok("Success: " + responseEntity.getBody().toString());
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                errorMsg = ((org.springframework.web.client.HttpClientErrorException) e).getResponseBodyAsString();
            }
            return ResponseEntity.status(500).body("Error: " + errorMsg);
        }
    }
}