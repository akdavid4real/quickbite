package com.debbiecyber.QuickBite.dto.response;


import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;

    private Long id;

    private String name;

    private String email;

    private UserRole  role;

    private AccountStatus accountStatus;

    private VerificationStatus verificationStatus;
}
