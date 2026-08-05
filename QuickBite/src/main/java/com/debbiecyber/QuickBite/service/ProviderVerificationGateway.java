package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ProviderVerificationRequest;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;

public interface ProviderVerificationGateway {
    VerificationDecision verify(ProviderVerificationRequest request, UserRole role);

    record VerificationDecision(
            VerificationStatus status,
            String reference,
            String failureReason
    ) {}
}
