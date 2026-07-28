package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.AuthResponse;
import com.debbiecyber.QuickBite.dto.resquest.LoginRequest;
import com.debbiecyber.QuickBite.dto.resquest.RegisterRequest;
import com.debbiecyber.QuickBite.entity.User;
import com.debbiecyber.QuickBite.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest  registerRequest) {
        AuthResponse  authResponse = authService.register(registerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("Registration successful", authResponse));
    }


    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse  authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(APIResponse.success("Login successful", authResponse));
    }
}
