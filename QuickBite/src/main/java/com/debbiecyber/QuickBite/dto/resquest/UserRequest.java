package com.debbiecyber.QuickBite.dto.resquest;


import com.debbiecyber.QuickBite.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String address;

    @NotNull(message = "Role is required")
    private UserRole role;
}
