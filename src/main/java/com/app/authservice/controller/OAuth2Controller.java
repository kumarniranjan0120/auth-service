package com.app.authservice.controller;

import com.app.authservice.dto.response.ApiResponse;
import com.app.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
//@Tag(name = "OAuth2", description = "OAuth2 Authentication Endpoints")
public class OAuth2Controller {

    private final AuthService authService;

    @GetMapping("/oauth2/authorize-url")
   // @Operation(summary = "Get OAuth2 authorization URL")
    public ResponseEntity<Map<String, String>> getOAuth2AuthorizationUrl(
            @RequestParam String provider,
            @RequestParam(required = false) String redirectUri) {

        Map<String, String> response = new HashMap<>();

        String authorizationUrl = switch (provider.toLowerCase()) {
            case "google" -> "/oauth2/authorize/google";
            case "github" -> "/oauth2/authorize/github";
            case "facebook" -> "/oauth2/authorize/facebook";
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };

        if (redirectUri != null && !redirectUri.isBlank()) {
            authorizationUrl += "?redirect_uri=" + redirectUri;
        }

        response.put("authorizationUrl", authorizationUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/providers")
   // @Operation(summary = "Get available OAuth2 providers")
    public ResponseEntity<Map<String, Boolean>> getAvailableOAuth2Providers() {
        Map<String, Boolean> providers = new HashMap<>();

        // Check if providers are configured
        providers.put("google", true); // You can check from configuration
        providers.put("github", true);
        providers.put("facebook", true);

        return ResponseEntity.ok(providers);
    }

    @PostMapping("/oauth2/unlink")
   // @Operation(summary = "Unlink OAuth2 provider from account")
    public ResponseEntity<ApiResponse> unlinkOAuth2Provider(
            @RequestParam String provider,
            HttpServletRequest request) {

        // Get current user
        var currentUser = authService.getCurrentUser();

        // Implement unlink logic here
        // This would remove the provider association from the user account

        return ResponseEntity.ok(ApiResponse.success(
                "Successfully unlinked " + provider + " from your account"
        ));
    }
}