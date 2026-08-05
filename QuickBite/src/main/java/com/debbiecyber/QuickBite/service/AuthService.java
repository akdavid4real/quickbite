package com.debbiecyber.QuickBite.service;


import com.debbiecyber.QuickBite.dto.response.AuthResponse;
import com.debbiecyber.QuickBite.dto.resquest.LoginRequest;
import com.debbiecyber.QuickBite.dto.resquest.RegisterRequest;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.enums.UserRole;
import com.debbiecyber.QuickBite.enums.AccountStatus;
import com.debbiecyber.QuickBite.enums.VerificationStatus;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import com.debbiecyber.QuickBite.repository.UserRepository;
import com.debbiecyber.QuickBite.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }
        if (registerRequest.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("You cannot register an admin");
        }
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        boolean provider = registerRequest.getRole() == UserRole.RESTAURANT_OWNER
                || registerRequest.getRole() == UserRole.RIDER;

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(hashedPassword)
                .phoneNumber(registerRequest.getPhoneNumber())
                .address(registerRequest.getAddress())
                .role(registerRequest.getRole())
                .accountStatus(provider ? AccountStatus.PENDING_APPROVAL : AccountStatus.ACTIVE)
                .verificationStatus(provider ? VerificationStatus.PENDING : VerificationStatus.NOT_REQUIRED)
                .build();
        User savedUser = userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getEmail())
                .password(savedUser.getPassword())
                .authorities("ROLE_" + savedUser.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .accountStatus(savedUser.getAccountStatus())
                .verificationStatus(savedUser.getVerificationStatus())
                .build();
    }


    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .verificationStatus(user.getVerificationStatus())
                .build();
    }
}
