package com.app.authservice.controller;

import com.app.authservice.dto.request.LoginRequest;
import com.app.authservice.dto.request.RegisterRequest;
import com.app.authservice.dto.request.TokenRefreshRequest;
import com.app.authservice.dto.response.ApiResponse;
import com.app.authservice.dto.response.AuthResponse;
import com.app.authservice.dto.response.UserResponse;
import com.app.authservice.model.User;
import com.app.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(Principal principal) {
        User currentUser = authService.getCurrentUser();
        authService.logoutUser(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(UserResponse.fromUser(currentUser));
    }
}