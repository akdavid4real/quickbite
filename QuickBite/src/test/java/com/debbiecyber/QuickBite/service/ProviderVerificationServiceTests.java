package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ProviderVerificationRequest;
import com.debbiecyber.QuickBite.entity.ProviderVerification;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import com.debbiecyber.QuickBite.repository.ProviderVerificationRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProviderVerificationServiceTests {
    @Test
    void verifiedMockResultStillRequiresAdminApproval() {
        UserRepository userRepository = mock(UserRepository.class);
        ProviderVerificationRepository repository = mock(ProviderVerificationRepository.class);
        ProviderVerificationGateway gateway = mock(ProviderVerificationGateway.class);
        ProviderVerificationService service = new ProviderVerificationService(userRepository, repository, gateway);
        User rider = User.builder().id(8L).email("rider@test.local").role(UserRole.RIDER).build();
        ProviderVerificationRequest request = new ProviderVerificationRequest();
        request.setLegalName("Test Rider"); request.setIdentityNumber("ID-123456"); request.setVehicleRegistrationNumber("LAG-123-AA");

        when(userRepository.findByEmail(rider.getEmail())).thenReturn(Optional.of(rider));
        when(repository.findByUserId(8L)).thenReturn(Optional.empty());
        when(gateway.verify(request, UserRole.RIDER)).thenReturn(new ProviderVerificationGateway.VerificationDecision(VerificationStatus.VERIFIED, "MOCK-KYC-TEST", null));
        when(repository.save(any(ProviderVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.submit(rider.getEmail(), request);

        assertEquals(VerificationStatus.VERIFIED, response.getVerificationStatus());
        assertEquals(AccountStatus.PENDING_APPROVAL, response.getAccountStatus());
        verify(userRepository).save(rider);
    }
}
