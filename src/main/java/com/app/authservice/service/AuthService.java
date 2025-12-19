package com.app.authservice.service;

import com.app.authservice.dto.request.LoginRequest;
import com.app.authservice.dto.request.RegisterRequest;
import com.app.authservice.dto.request.TokenRefreshRequest;
import com.app.authservice.dto.response.AuthResponse;
import com.app.authservice.dto.response.UserResponse;
import com.app.authservice.exception.AuthenticationException;
import com.app.authservice.exception.BadRequestException;
import com.app.authservice.exception.ResourceNotFoundException;
import com.app.authservice.exception.TokenRefreshException;
import com.app.authservice.model.RefreshToken;
import com.app.authservice.model.User;
import com.app.authservice.repo.UserRepository;
import com.app.authservice.security.JwtTokenProvider;
import com.app.authservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RoleService roleService;

    @Transactional
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() ->
                new ResourceNotFoundException("User", "username/email", loginRequest.getUsernameOrEmail()));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BadRequestException("Account is disabled");
        }

        if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
            throw new BadRequestException("Account is locked");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        user.setLastLoginAt(LocalDateTime.from(Instant.now()));
        userRepository.save(user);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .provider(User.AuthProvider.LOCAL)
                .emailVerified(false)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Assign default USER role
        user.addRole(roleService.getUserRole());

        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // ✅ Correct token generation
                    String accessToken = tokenProvider.generateToken(user);
                    return buildAuthResponse(accessToken, requestRefreshToken, user);
                })
                .orElseThrow(() -> new TokenRefreshException(
                        requestRefreshToken,
                        "Refresh token is invalid"
                ));
    }


    @Transactional
    public void logoutUser(String userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        }

        throw new AuthenticationException("Invalid authentication principal");
    }

    // Helper method to build AuthResponse
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        UserResponse userResponse = UserResponse.fromUser(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getExpiration())
                .user(userResponse)
                .build();
    }
}