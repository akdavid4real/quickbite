package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ProviderVerificationRequest;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class MockProviderVerificationGateway implements ProviderVerificationGateway {
    @Override
    public VerificationDecision verify(ProviderVerificationRequest request, UserRole role) {
        if (role == UserRole.RESTAURANT_OWNER && isBlank(request.getBusinessRegistrationNumber())) {
            throw new BadRequestException("A business registration number is required for restaurant owners");
        }
        if (role == UserRole.RIDER && isBlank(request.getVehicleRegistrationNumber())) {
            throw new BadRequestException("A vehicle registration number is required for riders");
        }

        String reference = "MOCK-KYC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT);
        boolean rejected = request.getIdentityNumber().toUpperCase(Locale.ROOT).contains("REJECT");
        return rejected
                ? new VerificationDecision(VerificationStatus.REJECTED, reference, "Mock provider could not verify the supplied identity")
                : new VerificationDecision(VerificationStatus.VERIFIED, reference, null);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
