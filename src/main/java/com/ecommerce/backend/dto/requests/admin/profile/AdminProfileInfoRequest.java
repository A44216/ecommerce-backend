package com.ecommerce.backend.dto.requests.admin.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProfileInfoRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    // Cho phép để trống HOẶC nếu nhập thì phải đúng định dạng Email
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Email(regexp = "^$|^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;

    // Cho phép để trống HOẶC nếu nhập thì phải là SĐT 10 số bắt đầu bằng số 0
    @Pattern(regexp = "^$|^0[0-9]{9}$", message = "Invalid phone number. Must be 10 digits starting with 0")
    private String phone;

    @Size(max = 255, message = "Avatar URL is too long")
    private String avatar;
}