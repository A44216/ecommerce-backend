package com.ecommerce.backend.dto.requests.seller.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerReplyRequest {

    @NotBlank(message = "Reply content is required")
    @Size(max = 1000, message = "Reply must be less than 1000 characters")
    private String sellerReply;
}