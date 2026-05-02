package com.ecommerce.backend.dto.requests.admin.complaint;

import com.ecommerce.backend.enums.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReplyComplaintRequest {

    @NotNull(message = "Status must not be null")
    private ComplaintStatus status;

    @NotBlank(message = "Response message must not be empty")
    @Size(max = 1000, message = "Response message must not exceed 1000 characters")
    private String response;
}
