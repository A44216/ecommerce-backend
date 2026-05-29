package com.ecommerce.backend;

import com.ecommerce.backend.dto.requests.AssistantChatRequest;
import com.ecommerce.backend.dto.requests.MessageContextDTO;
import com.ecommerce.backend.dto.responses.AssistantChatResponse;
import com.ecommerce.backend.service.CustomerAssistantService;
import com.ecommerce.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;

@SpringBootApplication
@Profile("test-gemini")
public class TestGeminiApp implements CommandLineRunner {

    @Autowired
    private CustomerAssistantService customerAssistantService;

    public static void main(String[] args) {
        SpringApplication.run(TestGeminiApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        AssistantChatRequest req = new AssistantChatRequest(new ArrayList<>(), "tìm cho tôi vài cái áo thun");
        AssistantChatResponse res = customerAssistantService.processChat(req);
        System.out.println("====== GEMINI RESPONSE ======");
        System.out.println("Type: " + res.getType());
        System.out.println("Text: " + res.getText());
        System.out.println("Data: " + (res.getData() != null ? "Yes" : "No") + " items");
        System.out.println("=============================");
        System.exit(0);
    }
}
