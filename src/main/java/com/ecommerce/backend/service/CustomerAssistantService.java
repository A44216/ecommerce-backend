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
import java.util.Locale;
import java.text.NumberFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;

@Service
public class CustomerAssistantService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public CustomerAssistantService(ProductService productService, 
                                    OrderRepository orderRepository, 
                                    UserRepository userRepository) {
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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
                    .text("Xin lỗi, tôi đang gặp sự cố kết nối: " + e.getMessage() + ". Vui lòng thử lại sau nhé! \uD83E\uDD16")
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
                "Sử dụng công cụ search_products khi khách hàng có nhu cầu tìm kiếm mua sắm. " +
                "Sử dụng công cụ track_order khi khách hàng muốn kiểm tra tình trạng đơn hàng.";

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

        Map<String, Object> trackOrderTool = new HashMap<>();
        trackOrderTool.put("name", "track_order");
        trackOrderTool.put("description", "Kiểm tra trạng thái đơn hàng. Nếu khách cung cấp mã đơn (ví dụ ORD-1234), hãy truyền mã đơn đó. Nếu khách chỉ hỏi chung chung 'đơn hàng của tôi đâu', truyền null.");
        Map<String, Object> trackParams = new HashMap<>();
        trackParams.put("type", "OBJECT");
        Map<String, Object> trackProps = new HashMap<>();
        Map<String, Object> orderCodeProp = new HashMap<>();
        orderCodeProp.put("type", "STRING");
        orderCodeProp.put("description", "Mã đơn hàng (nếu có)");
        trackProps.put("orderCode", orderCodeProp);
        trackParams.put("properties", trackProps);
        trackOrderTool.put("parameters", trackParams);

        Map<String, Object> functionDeclarations = new HashMap<>();
        functionDeclarations.put("functionDeclarations", List.of(searchProductsTool, trackOrderTool));

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
            } else if ("track_order".equals(functionName)) {
                Map<String, Object> args = (Map<String, Object>) functionCall.get("args");
                String orderCode = args != null && args.containsKey("orderCode") ? (String) args.get("orderCode") : null;

                return executeTrackOrder(orderCode);
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
        List<ProductResponse> products = productService.searchProducts(keyword, null);

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

    private AssistantChatResponse executeTrackOrder(String orderCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("🔐 Bạn cần **đăng nhập** để xem thông tin đơn hàng nhé!")
                    .build();
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("Xin lỗi, tôi không thể xác thực tài khoản của bạn.")
                    .build();
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        StringBuilder sb = new StringBuilder();

        if (orderCode != null && !orderCode.isEmpty()) {
            Order order = orderRepository.findByOrderCodeAndUserId(orderCode, user.getId()).orElse(null);
            if (order == null) {
                return AssistantChatResponse.builder()
                        .type("TEXT")
                        .text("Xin lỗi, tôi không tìm thấy đơn hàng **" + orderCode + "** nào của bạn.")
                        .build();
            }
            sb.append("📦 **Đơn hàng ").append(order.getOrderCode()).append("** của bạn đang ở trạng thái: **").append(translateStatus(order.getStatus().name())).append("**.\n");
            sb.append("Tổng tiền: ").append(formatter.format(order.getTotalPrice())).append("đ\n");
            sb.append("Sản phẩm:\n");
            for (OrderItem item : order.getItems()) {
                sb.append("- ").append(item.getProductName()).append(" (x").append(item.getQuantity()).append(")\n");
            }
        } else {
            List<Order> recentOrders = orderRepository.findTop3ByUserIdOrderByCreatedAtDesc(user.getId());
            if (recentOrders.isEmpty()) {
                return AssistantChatResponse.builder()
                        .type("TEXT")
                        .text("Bạn chưa có đơn hàng nào gần đây cả.")
                        .build();
            }
            sb.append("📝 Đây là **").append(recentOrders.size()).append("** đơn hàng gần nhất của bạn:\n\n");
            for (Order order : recentOrders) {
                sb.append("📦 **").append(order.getOrderCode()).append("** - ").append(translateStatus(order.getStatus().name())).append("\n");
                sb.append("   Tổng: ").append(formatter.format(order.getTotalPrice())).append("đ\n");
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    sb.append("   - ").append(order.getItems().get(0).getProductName());
                    if (order.getItems().size() > 1) {
                        sb.append(" và ").append(order.getItems().size() - 1).append(" sản phẩm khác");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
            sb.append("Bạn muốn hỏi chi tiết đơn nào không?");
        }

        return AssistantChatResponse.builder()
                .type("TEXT")
                .text(sb.toString().trim())
                .build();
    }

    private String translateStatus(String status) {
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng 🚚";
            case "COMPLETED": return "Đã giao thành công ✅";
            case "CANCELED": return "Đã hủy ❌";
            case "RETURN_REQUESTED": return "Yêu cầu trả hàng";
            case "RETURNED": return "Đã hoàn tiền";
            case "DISPUTED": return "Đang khiếu nại";
            default: return status;
        }
    }

    private AssistantChatResponse fallbackResponse() {
        return AssistantChatResponse.builder()
                .type("TEXT")
                .text("Xin lỗi, tôi chưa hiểu rõ ý của bạn. Bạn có thể nói rõ hơn không? \uD83E\uDD16")
                .build();
    }
}
