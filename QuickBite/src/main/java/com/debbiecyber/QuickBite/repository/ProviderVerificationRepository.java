package com.debbiecyber.QuickBite.repository;

import com.debbiecyber.QuickBite.entity.ProviderVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderVerificationRepository extends JpaRepository<ProviderVerification, Long> {
    Optional<ProviderVerification> findByUserId(Long userId);
}
