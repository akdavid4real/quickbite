package com.debbiecyber.QuickBite.controller;

import com.debbiecyber.QuickBite.dto.resquest.ProviderVerificationRequest;
import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.ProviderVerificationResponse;
import com.debbiecyber.QuickBite.service.ProviderVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/provider-verification")
@PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('RIDER')")
public class ProviderVerificationController {
    private final ProviderVerificationService verificationService;

    @GetMapping
    public ResponseEntity<APIResponse<ProviderVerificationResponse>> getMine(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(APIResponse.success("Verification fetched successfully", verificationService.getMine(principal.getUsername())));
    }

    @PostMapping
    public ResponseEntity<APIResponse<ProviderVerificationResponse>> submit(@Valid @RequestBody ProviderVerificationRequest request, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(APIResponse.success("Mock verification completed", verificationService.submit(principal.getUsername(), request)));
    }
}
