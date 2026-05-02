package com.ecommerce.backend.dto.requests.admin.complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReplyComplaintRequest {

    @NotBlank(message = "Response message must not be empty")
    @Size(max = 1000, message = "Response message must not exceed 1000 characters")
    private String response;
}
