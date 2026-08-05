package com.debbiecyber.QuickBite.dto.resquest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProviderVerificationRequest {
    @NotBlank(message = "Legal name is required")
    private String legalName;

    @NotBlank(message = "Identity number is required")
    @Size(min = 6, max = 40, message = "Identity number must be between 6 and 40 characters")
    private String identityNumber;

    private String businessRegistrationNumber;
    private String vehicleRegistrationNumber;
}
