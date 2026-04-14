package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.requests.ChatbotResponseRequest;
import com.ecommerce.backend.dto.responses.ChatbotResponseDTO;
import com.ecommerce.backend.entity.ChatbotResponse;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ChatbotResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatbotResponseService {

    private final ChatbotResponseRepository repository;

    public ChatbotResponseService(ChatbotResponseRepository repository) {
        this.repository = repository;
    }

    private ChatbotResponseDTO mapToDTO(ChatbotResponse chatbot) {
        return new ChatbotResponseDTO(
                chatbot.getId(),
                chatbot.getKeyword(),
                chatbot.getResponse()
        );
    }

    private ChatbotResponse getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Chatbot response not found with id: " + id));
    }

    private void mapRequestToEntity(ChatbotResponseRequest request, ChatbotResponse chatbot) {
        chatbot.setKeyword(request.getKeyword().trim());
        chatbot.setResponse(request.getResponse().trim());
    }

    @Transactional(readOnly = true)
    public List<ChatbotResponseDTO> getAllResponses() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatbotResponseDTO getByKeyword(String keyword) {

        String normalizedKeyword = keyword.trim();

        ChatbotResponse chatbot = repository
                .findByKeywordIgnoreCase(normalizedKeyword)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Keyword not found"));

        return mapToDTO(chatbot);
    }

    @Transactional
    public ChatbotResponseDTO create(ChatbotResponseRequest request) {

        String keyword = request.getKeyword().trim();

        if (repository.existsByKeywordIgnoreCase(keyword)) {
            throw new BadRequestException("Keyword already exists");
        }

        ChatbotResponse chatbot = new ChatbotResponse();

        mapRequestToEntity(request, chatbot);

        return mapToDTO(repository.save(chatbot));
    }

    @Transactional
    public ChatbotResponseDTO update(Integer id, ChatbotResponseRequest request) {

        ChatbotResponse chatbot = getOrThrow(id);

        String keyword = request.getKeyword().trim();

        if (repository.existsByKeywordIgnoreCaseAndIdNot(keyword, id)) {
            throw new BadRequestException("Keyword already exists");
        }

        mapRequestToEntity(request, chatbot);

        return mapToDTO(repository.save(chatbot));
    }

    @Transactional
    public void delete(Integer id) {
        repository.delete(getOrThrow(id));
    }
}