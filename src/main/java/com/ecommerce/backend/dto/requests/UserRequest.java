package com.ecommerce.backend.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.ecommerce.backend.enums.Role;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be less than 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can only contain letters, numbers and underscores"
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Invalid phone number")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;
}