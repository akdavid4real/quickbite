package com.debbiecyber.QuickBite.dto.resquest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginRequest {
    @NotBlank(message = "email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}
