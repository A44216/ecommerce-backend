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
            String errorMsg = e.getMessage();
            String userFriendlyMessage = "Xin lỗi, tôi đang gặp sự cố kết nối. Vui lòng thử lại sau nhé! \uD83E\uDD16";
            
            if (errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("Too Many Requests") || errorMsg.contains("quota"))) {
                userFriendlyMessage = "Hệ thống AI đang bị quá tải (vượt quá giới hạn miễn phí). Bạn vui lòng đợi khoảng 1 phút rồi thử lại nhé! \uD83E\uDD16";
            }
            
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text(userFriendlyMessage)
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
                "KHI khách hàng hỏi mua, tìm kiếm sản phẩm: BẮT BUỘC PHẢI GỌI HÀM search_products NGAY LẬP TỨC. KHÔNG ĐƯỢC CHAT HAY HỎI LẠI TRƯỚC KHI GỌI HÀM. " +
                "KHI khách hàng hỏi về sản phẩm bán chạy nhất, hot nhất: BẮT BUỘC GỌI HÀM get_trending_products. " +
                "KHI khách hàng hỏi gợi ý sản phẩm (gợi ý cho tôi): BẮT BUỘC GỌI HÀM recommend_products. " +
                "KHI khách muốn kiểm tra đơn hàng: BẮT BUỘC GỌI HÀM track_order.";

        List<MessageContextDTO> history = request.getHistory();
        // Tối ưu hóa: Chỉ lấy tối đa 6 tin nhắn gần nhất (3 lượt trao đổi) để tiết kiệm Token
        int maxHistorySize = 6;
        int startIndex = Math.max(0, history.size() - maxHistorySize);

        for (int i = startIndex; i < history.size(); i++) {
            MessageContextDTO msg = history.get(i);
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
        searchProductsTool.put("description", "BẮT BUỘC phải gọi ngay lập tức khi khách hàng muốn tìm kiếm hoặc hỏi mua một sản phẩm. Tự động điền tham số keyword. Tham số up_sell_message dùng để gửi lời nhắn thân thiện và gợi ý mua kèm (VD: 'Tôi tìm thấy điện thoại cho bạn, bạn có muốn xem thêm ốp lưng không?'). Tuyệt đối KHÔNG tự trả lời văn bản mà phải gọi hàm này để hệ thống hiển thị sản phẩm.");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "OBJECT");
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> keywordProp = new HashMap<>();
        keywordProp.put("type", "STRING");
        keywordProp.put("description", "Từ khóa tìm kiếm sản phẩm. Ví dụ: 'áo thun', 'điện thoại'");
        properties.put("keyword", keywordProp);
        
        Map<String, Object> upSellProp = new HashMap<>();
        upSellProp.put("type", "STRING");
        upSellProp.put("description", "Câu hỏi thân thiện mời khách mua thêm sản phẩm phụ trợ.");
        properties.put("up_sell_message", upSellProp);
        
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

        Map<String, Object> compareTool = new HashMap<>();
        compareTool.put("name", "compare_products");
        compareTool.put("description", "So sánh 2 sản phẩm. Sử dụng khi khách hàng phân vân giữa 2 sản phẩm.");
        Map<String, Object> compareParams = new HashMap<>();
        compareParams.put("type", "OBJECT");
        Map<String, Object> compareProps = new HashMap<>();
        Map<String, Object> kw1Prop = new HashMap<>();
        kw1Prop.put("type", "STRING");
        kw1Prop.put("description", "Từ khóa sản phẩm thứ nhất");
        Map<String, Object> kw2Prop = new HashMap<>();
        kw2Prop.put("type", "STRING");
        kw2Prop.put("description", "Từ khóa sản phẩm thứ hai");
        compareProps.put("keyword1", kw1Prop);
        compareProps.put("keyword2", kw2Prop);
        compareParams.put("properties", compareProps);
        compareParams.put("required", List.of("keyword1", "keyword2"));
        compareTool.put("parameters", compareParams);

        Map<String, Object> recommendTool = new HashMap<>();
        recommendTool.put("name", "recommend_products");
        recommendTool.put("description", "Gợi ý các sản phẩm phù hợp với sở thích cá nhân của khách hàng dựa trên lịch sử mua.");
        
        Map<String, Object> trendingTool = new HashMap<>();
        trendingTool.put("name", "get_trending_products");
        trendingTool.put("description", "Lấy danh sách các sản phẩm bán chạy nhất, hot nhất, được mua nhiều nhất hiện nay.");

        Map<String, Object> functionDeclarations = new HashMap<>();
        functionDeclarations.put("functionDeclarations", List.of(searchProductsTool, trackOrderTool, compareTool, recommendTool, trendingTool));

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
                String upSell = args != null && args.containsKey("up_sell_message") ? (String) args.get("up_sell_message") : null;

                return executeSearchProducts(keyword, upSell);
            } else if ("track_order".equals(functionName)) {
                Map<String, Object> args = (Map<String, Object>) functionCall.get("args");
                String orderCode = args != null && args.containsKey("orderCode") ? (String) args.get("orderCode") : null;

                return executeTrackOrder(orderCode);
            } else if ("compare_products".equals(functionName)) {
                Map<String, Object> args = (Map<String, Object>) functionCall.get("args");
                String keyword1 = args != null && args.containsKey("keyword1") ? (String) args.get("keyword1") : "";
                String keyword2 = args != null && args.containsKey("keyword2") ? (String) args.get("keyword2") : "";

                return executeCompareProducts(keyword1, keyword2);
            } else if ("recommend_products".equals(functionName)) {
                return executeRecommendProducts();
            } else if ("get_trending_products".equals(functionName)) {
                return executeTrendingProducts();
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

    private AssistantChatResponse executeSearchProducts(String keyword, String upSellMessage) {
        // Thực thi logic Backend (tìm kiếm) thông qua ProductService
        List<ProductResponse> products = productService.searchProducts(keyword, null);

        List<ProductResponse> topProducts = products.stream()
                .limit(10) // Trả về tối đa 10 sp cho Carousel
                .toList();

        String responseText = "Xin lỗi, tôi không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn. Bạn thử từ khóa khác nhé!";
        
        if (!topProducts.isEmpty()) {
            if (upSellMessage != null && !upSellMessage.isEmpty()) {
                responseText = "Tôi đã tìm thấy một số sản phẩm phù hợp.\n\n" + upSellMessage;
            } else {
                responseText = "Tôi đã tìm thấy một số sản phẩm phù hợp. Bạn tham khảo nhé!";
            }
        }

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

    private AssistantChatResponse executeCompareProducts(String keyword1, String keyword2) {
        List<ProductResponse> p1List = productService.searchProducts(keyword1, null);
        List<ProductResponse> p2List = productService.searchProducts(keyword2, null);
        
        if (p1List.isEmpty() || p2List.isEmpty()) {
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("Xin lỗi, tôi không tìm đủ sản phẩm để so sánh. Bạn thử từ khóa khác rõ ràng hơn nhé!")
                    .build();
        }
        
        ProductResponse p1 = p1List.get(0);
        ProductResponse p2 = p2List.get(0);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Dưới đây là bảng so sánh giữa **").append(p1.getName()).append("** và **").append(p2.getName()).append("**:\n\n");
        sb.append("| Tiêu chí | ").append(p1.getName()).append(" | ").append(p2.getName()).append(" |\n");
        sb.append("| :--- | :--- | :--- |\n");
        sb.append("| **Giá** | ").append(formatter.format(p1.getPrice())).append("đ | ").append(formatter.format(p2.getPrice())).append("đ |\n");
        sb.append("| **Đánh giá** | ").append(p1.getRatingAvg()).append("⭐ (").append(p1.getRatingCount()).append(") | ").append(p2.getRatingAvg()).append("⭐ (").append(p2.getRatingCount()).append(") |\n");
        sb.append("| **Đã bán** | ").append(p1.getSoldCount()).append(" | ").append(p2.getSoldCount()).append(" |\n");
        sb.append("| **Tồn kho** | ").append(p1.getStock()).append(" | ").append(p2.getStock()).append(" |\n");
        sb.append("| **Cửa hàng** | ").append(p1.getShopName()).append(" | ").append(p2.getShopName()).append(" |\n");
        
        return AssistantChatResponse.builder()
                .type("TEXT")
                .text(sb.toString())
                .build();
    }

    private AssistantChatResponse executeRecommendProducts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("🔐 Bạn cần **đăng nhập** để tôi có thể gợi ý sản phẩm dựa trên sở thích của bạn nhé!")
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
        
        Integer favCategoryId = orderRepository.findFavoriteCategoryIdByUserId(user.getId()).orElse(null);
        if (favCategoryId == null) {
            return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("Bạn chưa mua món đồ nào gần đây nên tôi chưa biết sở thích của bạn. Bạn hãy thử tìm kiếm sản phẩm trước nhé!")
                    .build();
        }
        
        List<ProductResponse> products = productService.getProductsByCategory(favCategoryId);
        
        List<ProductResponse> topProducts = products.stream()
                .limit(10)
                .toList();
                
        if (topProducts.isEmpty()) {
             return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("Rất tiếc, hiện tại không có sản phẩm mới nào trong danh mục yêu thích của bạn.")
                    .build();
        }
        
        return AssistantChatResponse.builder()
                .type("PRODUCT_CAROUSEL")
                .text("Dựa vào lịch sử mua hàng, tôi thấy bạn rất quan tâm đến danh mục **" + topProducts.get(0).getCategoryName() + "**! Dưới đây là những sản phẩm dành riêng cho bạn:")
                .data(topProducts)
                .build();
    }

    private AssistantChatResponse executeTrendingProducts() {
        List<ProductResponse> topProducts = productService.getTrendingProducts();
        
        if (topProducts == null || topProducts.isEmpty()) {
             return AssistantChatResponse.builder()
                    .type("TEXT")
                    .text("Rất tiếc, hiện tại không có sản phẩm nào nổi bật. Bạn thử tìm kiếm sản phẩm cụ thể nhé!")
                    .build();
        }
        
        return AssistantChatResponse.builder()
                .type("PRODUCT_CAROUSEL")
                .text("Đây là những sản phẩm đang bán chạy nhất và được nhiều khách hàng yêu thích hiện nay. Bạn tham khảo nhé!")
                .data(topProducts)
                .build();
    }

    private AssistantChatResponse fallbackResponse() {
        return AssistantChatResponse.builder()
                .type("TEXT")
                .text("Xin lỗi, tôi chưa hiểu rõ ý của bạn. Bạn có thể nói rõ hơn không? \uD83E\uDD16")
                .build();
    }
}
