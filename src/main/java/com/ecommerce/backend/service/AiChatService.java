package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Shop;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    public AiChatService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.restTemplate = new RestTemplate();
    }

    public String generateResponse(Shop shop, String userMessage) {
        try {
            String prompt = buildPrompt(shop, userMessage);
            return callGemini(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng đợi Seller quay lại nhé! 🤖";
        }
    }

    private String buildPrompt(Shop shop, String userMessage) {
        // Lấy 5 sản phẩm mới nhất của shop để làm context
        List<Product> products = productRepository.findTop5ByShopIdAndIsDeletedFalseAndStatusOrderByCreatedAtDesc(
                shop.getId(), 
                com.ecommerce.backend.enums.ProductStatus.APPROVED
        );

        StringBuilder context = new StringBuilder();
        context.append("Bạn là trợ lý ảo AI thông minh của shop '").append(shop.getShopName()).append("'.\n");
        context.append("Mô tả shop: ").append(shop.getDescription() != null ? shop.getDescription() : "Chưa có mô tả").append(".\n");
        context.append("Dưới đây là một số sản phẩm nổi bật của shop:\n");

        for (Product p : products) {
            context.append("- ").append(p.getName()).append(": ").append(p.getPrice()).append(" VNĐ. Mô tả: ").append(p.getDescription()).append("\n");
        }

        context.append("\nQuy tắc trả lời:\n");
        context.append("1. Trả lời lịch sự, thân thiện, xưng hô phù hợp với khách hàng.\n");
        context.append("2. Nếu khách hàng hỏi về sản phẩm, hãy dựa trên danh sách trên để tư vấn.\n");
        context.append("3. Nếu không có thông tin hoặc khách hỏi vấn đề phức tạp, hãy bảo khách đợi Seller thật sự quay lại hỗ trợ.\n");
        context.append("4. Câu trả lời nên ngắn gọn, súc tích.\n");
        context.append("5. Luôn thêm icon 🤖 ở cuối câu trả lời để khách biết đây là hệ thống tự động.\n");

        context.append("\nTin nhắn của khách hàng: ").append(userMessage);

        return context.toString();
    }

    private String callGemini(String prompt) {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Cấu trúc request body cho Gemini API
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println(">>> Đang gọi Gemini API với Prompt:\n" + prompt);
            org.springframework.http.ResponseEntity<Map> responseEntity = 
                restTemplate.postForEntity(url, request, Map.class);
            
            Map<String, Object> response = responseEntity.getBody();
            System.out.println(">>> Kết quả Gemini API: " + responseEntity.getStatusCode());
            System.out.println(">>> Response Body: " + response);

            // Parse response (đơn giản hóa)
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) candidate.get("content");
                    if (resContent != null && resContent.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) resContent.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String aiText = (String) parts.get(0).get("text");
                            return aiText.trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(">>> LỖI GỌI GEMINI API: " + e.getMessage());
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                String errorBody = ((org.springframework.web.client.HttpClientErrorException) e).getResponseBodyAsString();
                System.err.println(">>> CHI TIẾT LỖI TỪ GOOGLE: " + errorBody);
            }
        }

        return "Cảm ơn bạn đã nhắn tin! Shop đã nhận được thông tin và sẽ phản hồi sớm nhất có thể. 🤖";
    }
}
