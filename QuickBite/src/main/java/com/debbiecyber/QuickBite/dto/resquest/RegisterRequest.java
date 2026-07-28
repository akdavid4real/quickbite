package com.debbiecyber.QuickBite.dto.resquest;

import com.debbiecyber.QuickBite.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "password must be at least 6 characters")
    private String password;

    @NotBlank(message = "phone number is required")
    private String phoneNumber;

    private String address;

    @NotNull(message = "role is required")
    private UserRole role;
}
