package com.app.authservice.dto.response;

import com.app.authservice.model.User;
import lombok.Builder;
import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;  // This should be UserResponse

    @Builder
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.tokenType = "Bearer";
    }

    // Factory method if you need User parameter
    public static AuthResponse fromUser(String accessToken, String refreshToken, Long expiresIn, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(UserResponse.fromUser(user))
                .build();
    }
}