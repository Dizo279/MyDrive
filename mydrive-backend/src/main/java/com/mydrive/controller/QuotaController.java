package com.mydrive.controller;

import com.mydrive.dto.response.QuotaResponse;
import com.mydrive.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quota")
@RequiredArgsConstructor
public class QuotaController {

    private final QuotaService quotaService;

    // GET /api/quota/me
    @GetMapping("/me")
    public ResponseEntity<QuotaResponse> getMyQuota(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                quotaService.getMyQuota(userDetails.getUsername())
        );
    }

    // PUT /api/quota/upgrade?plan=PRO
    @PutMapping("/upgrade")
    public ResponseEntity<QuotaResponse> upgradePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String plan) {
        return ResponseEntity.ok(
                quotaService.upgradePlan(userDetails.getUsername(), plan)
        );
    }
}