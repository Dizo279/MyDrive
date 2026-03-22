package com.mydrive.controller;

import com.mydrive.dto.request.UpdateProfileRequest;
import com.mydrive.dto.response.AuthResponse;
import com.mydrive.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // GET /api/profile
    @GetMapping
    public ResponseEntity<AuthResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                profileService.getProfile(userDetails.getUsername())
        );
    }

    // PUT /api/profile
    @PutMapping
    public ResponseEntity<AuthResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                profileService.updateProfile(userDetails.getUsername(), request)
        );
    }
}