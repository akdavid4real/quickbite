package com.debbiecyber.QuickBite.dto.response;

import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProviderVerificationResponse {
    private VerificationStatus verificationStatus;
    private AccountStatus accountStatus;
    private String providerReference;
    private String failureReason;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
}
