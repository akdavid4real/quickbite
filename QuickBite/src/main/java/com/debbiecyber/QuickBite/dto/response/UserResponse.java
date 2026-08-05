package com.debbiecyber.QuickBite.dto.response;


import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private UserRole role;

    private AccountStatus accountStatus;

    private VerificationStatus verificationStatus;

    private Boolean availableForDelivery;

    private String address;

    private LocalDateTime createdAt;
}
