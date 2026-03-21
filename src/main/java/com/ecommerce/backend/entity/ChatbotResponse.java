package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "chatbot_responses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_chatbot_responses_keyword",
                        columnNames = "keyword"
                )
        }
)
@Getter
@Setter
public class ChatbotResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;
}