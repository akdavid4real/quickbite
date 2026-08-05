package com.debbiecyber.QuickBite.entity;


import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'ACTIVE'")
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'NOT_REQUIRED'")
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.NOT_REQUIRED;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean availableForDelivery = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
