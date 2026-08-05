package com.debbiecyber.QuickBite.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedAddressResponse {
    private Long id;
    private String label;
    private String address;
    private Boolean isDefault;
}
