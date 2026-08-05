package com.debbiecyber.QuickBite.controller;

import com.debbiecyber.QuickBite.dto.resquest.ProfileRequest;
import com.debbiecyber.QuickBite.dto.resquest.SavedAddressRequest;
import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.SavedAddressResponse;
import com.debbiecyber.QuickBite.dto.response.UserResponse;
import com.debbiecyber.QuickBite.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<APIResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(APIResponse.success("Profile fetched successfully", profileService.getProfile(principal.getUsername())));
    }

    @PutMapping
    public ResponseEntity<APIResponse<UserResponse>> updateProfile(@Valid @RequestBody ProfileRequest request, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(APIResponse.success("Profile updated successfully", profileService.updateProfile(principal.getUsername(), request)));
    }

    @GetMapping("/addresses")
    public ResponseEntity<APIResponse<List<SavedAddressResponse>>> getAddresses(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(APIResponse.success("Saved addresses fetched successfully", profileService.getAddresses(principal.getUsername())));
    }

    @PostMapping("/addresses")
    public ResponseEntity<APIResponse<SavedAddressResponse>> createAddress(@Valid @RequestBody SavedAddressRequest request, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success("Address saved successfully", profileService.createAddress(principal.getUsername(), request)));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<APIResponse<SavedAddressResponse>> updateAddress(@PathVariable Long id, @Valid @RequestBody SavedAddressRequest request, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(APIResponse.success("Address updated successfully", profileService.updateAddress(id, principal.getUsername(), request)));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<APIResponse<Void>> deleteAddress(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        profileService.deleteAddress(id, principal.getUsername());
        return ResponseEntity.ok(APIResponse.success("Address deleted successfully"));
    }
}
