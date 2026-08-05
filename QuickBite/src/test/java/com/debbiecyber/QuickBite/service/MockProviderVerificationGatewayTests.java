package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ProviderVerificationRequest;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockProviderVerificationGatewayTests {
    private final MockProviderVerificationGateway gateway = new MockProviderVerificationGateway();

    @Test
    void verifiesStructurallyValidRider() {
        ProviderVerificationRequest request = request("ID-123456");
        request.setVehicleRegistrationNumber("LAG-123-AA");

        ProviderVerificationGateway.VerificationDecision decision = gateway.verify(request, UserRole.RIDER);

        assertEquals(VerificationStatus.VERIFIED, decision.status());
        assertTrue(decision.reference().startsWith("MOCK-KYC-"));
    }

    @Test
    void supportsDeterministicRejectedPath() {
        ProviderVerificationRequest request = request("REJECT-123456");
        request.setBusinessRegistrationNumber("RC-123456");

        ProviderVerificationGateway.VerificationDecision decision = gateway.verify(request, UserRole.RESTAURANT_OWNER);

        assertEquals(VerificationStatus.REJECTED, decision.status());
    }

    @Test
    void requiresRoleSpecificEvidence() {
        assertThrows(BadRequestException.class, () -> gateway.verify(request("ID-123456"), UserRole.RIDER));
        assertThrows(BadRequestException.class, () -> gateway.verify(request("ID-123456"), UserRole.RESTAURANT_OWNER));
    }

    private ProviderVerificationRequest request(String identityNumber) {
        ProviderVerificationRequest request = new ProviderVerificationRequest();
        request.setLegalName("Test Provider");
        request.setIdentityNumber(identityNumber);
        return request;
    }
}
