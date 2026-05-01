package com.ecommerce.backend.dto.responses.seller.review;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SellerReviewResponse {

    // REVIEW ID
    private Integer reviewId;

    // USER INFO
    private String fullName;
    private String userAvatar;

    // REVIEW DATA
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    // SELLER REPLY
    private String sellerReply;
    private LocalDateTime sellerReplyAt;

    // STATUS (for filter + paging)
    private Boolean isReplied;
}