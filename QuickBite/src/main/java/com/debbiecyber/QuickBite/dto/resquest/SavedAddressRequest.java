package com.debbiecyber.QuickBite.dto.resquest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavedAddressRequest {
    @NotBlank(message = "Address label is required")
    @Size(max = 40, message = "Address label must be at most 40 characters")
    private String label;

    @NotBlank(message = "Address is required")
    @Size(min = 8, max = 300, message = "Address must be between 8 and 300 characters")
    private String address;

    private boolean isDefault;
}
