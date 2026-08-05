package com.debbiecyber.QuickBite.dto.resquest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryProofRequest {
    @NotBlank(message = "Evidence URL is required")
    @Size(max = 1000, message = "Evidence URL is too long")
    private String evidenceUrl;

    @Size(max = 1000, message = "Delivery notes are too long")
    private String notes;
}
