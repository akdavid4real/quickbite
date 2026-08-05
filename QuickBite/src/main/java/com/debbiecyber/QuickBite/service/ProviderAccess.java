package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import com.debbiecyber.QuickBite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("providerAccess")
@RequiredArgsConstructor
public class ProviderAccess {
    private final UserRepository userRepository;

    public boolean canOperate(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getAccountStatus() == AccountStatus.ACTIVE
                        && (user.getRole() == UserRole.CUSTOMER
                        || user.getRole() == UserRole.ADMIN
                        || user.getVerificationStatus() == VerificationStatus.VERIFIED))
                .orElse(false);
    }
}
