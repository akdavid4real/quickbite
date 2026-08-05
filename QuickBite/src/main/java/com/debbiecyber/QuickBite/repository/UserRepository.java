package com.debbiecyber.QuickBite.repository;

import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByAccountStatusAndVerificationStatus(
            AccountStatus accountStatus,
            VerificationStatus verificationStatus
    );

    Page<User> findByAccountStatusAndVerificationStatusAndRoleIn(
            AccountStatus accountStatus,
            VerificationStatus verificationStatus,
            List<UserRole> roles,
            Pageable pageable
    );
}
