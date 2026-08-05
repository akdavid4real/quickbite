package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.dto.resquest.ProviderVerificationRequest;
import com.debbiecyber.QuickBite.dto.response.ProviderVerificationResponse;
import com.debbiecyber.QuickBite.entity.ProviderVerification;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.exceptions.ResourceNotFoundException;
import com.debbiecyber.QuickBite.repository.ProviderVerificationRepository;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProviderVerificationService {
    private final UserRepository userRepository;
    private final ProviderVerificationRepository verificationRepository;
    private final ProviderVerificationGateway verificationGateway;

    @Transactional
    public ProviderVerificationResponse submit(String email, ProviderVerificationRequest request) {
        User user = getProvider(email);
        ProviderVerificationGateway.VerificationDecision decision = verificationGateway.verify(request, user.getRole());
        LocalDateTime now = LocalDateTime.now();

        ProviderVerification verification = verificationRepository.findByUserId(user.getId())
                .orElseGet(() -> ProviderVerification.builder().user(user).build());
        verification.setLegalName(request.getLegalName());
        verification.setIdentityNumber(request.getIdentityNumber());
        verification.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        verification.setVehicleRegistrationNumber(request.getVehicleRegistrationNumber());
        verification.setStatus(decision.status());
        verification.setProviderReference(decision.reference());
        verification.setFailureReason(decision.failureReason());
        verification.setSubmittedAt(now);
        verification.setVerifiedAt(decision.status() == com.debbiecyber.QuickBite.enums.VerificationStatus.VERIFIED ? now : null);
        verificationRepository.save(verification);

        user.setVerificationStatus(decision.status());
        user.setAccountStatus(AccountStatus.PENDING_APPROVAL);
        userRepository.save(user);
        return map(verification, user);
    }

    public ProviderVerificationResponse getMine(String email) {
        User user = getProvider(email);
        return verificationRepository.findByUserId(user.getId())
                .map(verification -> map(verification, user))
                .orElse(ProviderVerificationResponse.builder()
                        .verificationStatus(user.getVerificationStatus())
                        .accountStatus(user.getAccountStatus())
                        .build());
    }

    private User getProvider(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != UserRole.RESTAURANT_OWNER && user.getRole() != UserRole.RIDER) {
            throw new BadRequestException("Verification is only available to restaurant owners and riders");
        }
        return user;
    }

    private ProviderVerificationResponse map(ProviderVerification verification, User user) {
        return ProviderVerificationResponse.builder()
                .verificationStatus(verification.getStatus())
                .accountStatus(user.getAccountStatus())
                .providerReference(verification.getProviderReference())
                .failureReason(verification.getFailureReason())
                .submittedAt(verification.getSubmittedAt())
                .verifiedAt(verification.getVerifiedAt())
                .build();
    }
}
