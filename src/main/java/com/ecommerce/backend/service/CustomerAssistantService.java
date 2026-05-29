package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.AssistantChatRequest;
import com.ecommerce.backend.dto.requests.MessageContextDTO;
import com.ecommerce.backend.dto.responses.AssistantChatResponse;
import com.ecommerce.backend.dto.responses.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerAssistantService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ProductService productService;
    private final RestTemplate restTemplate;

    public CustomerAssistantService(ProductService productService) {
        this.productService = productService;
        this.restTemplate = new RestTemplate();
    }

    public AssistantChatResponse processChat(AssistantChatRequest request) {
        try {
            Map<String, Object> requestBody = buildGeminiRequest(request);
            String url = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<Map> responseEntity =
                    restTemplate.postForEntity(url, entity, Map.class);

            return parseGeminiResponseAndExecuteTool(responseEntity.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("Xin lỗi, tôi đang gặp sự cố kết nối. Bạn vui lòng thử lại sau nhé! \uD83E\uDD16")
                    .build();
        }
    }

    private Map<String, Object> buildGeminiRequest(AssistantChatRequest request) {
        Map<String, Object> body = new HashMap<>();

        // 1. Build Contents (History + Current Message)
        List<Map<String, Object>> contents = new ArrayList<>();

        // System Instruction can be added as a separate context or first message, but Gemini 1.5+ supports system_instruction
        // For simplicity with generateContent, we'll just prepend it to the first user message if history is empty
        String systemInstruction = "Bạn là trợ lý mua sắm ảo thông minh của ứng dụng E-commerce. " +
                "Nhiệm vụ của bạn là tư vấn sản phẩm cho khách hàng. " +
                "Luôn xưng hô lịch sự, thân thiện và trả lời ngắn gọn. " +
                "Sử dụng công cụ search_products khi khách hàng có nhu cầu tìm kiếm mua sắm.";

        for (MessageContextDTO msg : request.getHistory()) {
            Map<String, Object> content = new HashMap<>();
            content.put("role", msg.getRole().equals("assistant") ? "model" : "user");
            content.put("parts", List.of(Map.of("text", msg.getContent())));
            contents.add(content);
        }

        // Add current message
        String finalMessage = contents.isEmpty() ? systemInstruction + "\n\nKhách hàng: " + request.getMessage() : request.getMessage();
        Map<String, Object> currentContent = new HashMap<>();
        currentContent.put("role", "user");
        currentContent.put("parts", List.of(Map.of("text", finalMessage)));
        contents.add(currentContent);

        body.put("contents", contents);

        // 2. Build Tools (Function Calling)
        Map<String, Object> searchProductsTool = new HashMap<>();
        searchProductsTool.put("name", "search_products");
        searchProductsTool.put("description", "Tìm kiếm sản phẩm dựa trên từ khóa (keyword). Sử dụng khi khách hàng muốn tìm mua đồ.");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "OBJECT");

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> keywordProp = new HashMap<>();
        keywordProp.put("type", "STRING");
        keywordProp.put("description", "Từ khóa tìm kiếm sản phẩm. Ví dụ: 'áo thun', 'điện thoại'");
        properties.put("keyword", keywordProp);

        parameters.put("properties", properties);
        parameters.put("required", List.of("keyword"));

        searchProductsTool.put("parameters", parameters);

        Map<String, Object> functionDeclarations = new HashMap<>();
        functionDeclarations.put("function_declarations", List.of(searchProductsTool)); // fixed key

        body.put("tools", List.of(functionDeclarations));

        return body;
    }

    private AssistantChatResponse parseGeminiResponseAndExecuteTool(Map<String, Object> response) {
        if (response == null || !response.containsKey("candidates")) {
            return fallbackResponse();
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return fallbackResponse();
        }

        Map<String, Object> candidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        if (content == null || !content.containsKey("parts")) {
             return fallbackResponse();
        }
        
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return fallbackResponse();
        }

        // Kiểm tra xem part trả về là TEXT hay FUNCTION_CALL
        Map<String, Object> firstPart = parts.get(0);

        if (firstPart.containsKey("functionCall")) {
            // Đã gọi hàm
            Map<String, Object> functionCall = (Map<String, Object>) firstPart.get("functionCall");
            String functionName = (String) functionCall.get("name");

            if ("search_products".equals(functionName)) {
                Map<String, Object> args = (Map<String, Object>) functionCall.get("args");
                String keyword = args != null && args.containsKey("keyword") ? (String) args.get("keyword") : "";

                return executeSearchProducts(keyword);
            }
        } else if (firstPart.containsKey("text")) {
            // Trả lời văn bản bình thường
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text(((String) firstPart.get("text")).trim())
                    .build();
        }

        return fallbackResponse();
    }

    private AssistantChatResponse executeSearchProducts(String keyword) {
        // Thực thi logic Backend (tìm kiếm) thông qua ProductService
        List<ProductResponse> products = productService.searchProducts(keyword);

        List<ProductResponse> topProducts = products.stream()
                .limit(10) // Trả về tối đa 10 sp cho Carousel
                .toList();

        String responseText = topProducts.isEmpty() 
                ? "Xin lỗi, tôi không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn. Bạn thử từ khóa khác nhé!"
                : "Tôi đã tìm thấy một số sản phẩm phù hợp. Bạn tham khảo nhé!";

        return AssistantChatResponse.builder()
                .type(topProducts.isEmpty() ? "TEXT" : "PRODUCT_CAROUSEL")
                .text(responseText)
                .data(topProducts.isEmpty() ? null : topProducts)
                .build();
    }

    private AssistantChatResponse fallbackResponse() {
        return AssistantChatResponse.builder()
                .type("TEXT")
                .text("Xin lỗi, tôi chưa hiểu rõ ý của bạn. Bạn có thể nói rõ hơn không? \uD83E\uDD16")
                .build();
    }
}
