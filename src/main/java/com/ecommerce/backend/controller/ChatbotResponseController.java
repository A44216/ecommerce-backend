package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.requests.ChatbotResponseRequest;
import com.ecommerce.backend.dto.responses.ChatbotResponseDTO;
import com.ecommerce.backend.service.ChatbotResponseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin
public class ChatbotResponseController {

    private final ChatbotResponseService service;

    public ChatbotResponseController(ChatbotResponseService service) {
        this.service = service;
    }

    @GetMapping
    public List<ChatbotResponseDTO> getAll() {
        return service.getAllResponses();
    }

    @GetMapping("/ask")
    public ChatbotResponseDTO ask(@RequestParam(required = true) String keyword) {
        return service.getByKeyword(keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatbotResponseDTO create(@Valid @RequestBody ChatbotResponseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ChatbotResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody ChatbotResponseRequest request) {

        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}