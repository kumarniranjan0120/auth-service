package com.app.authservice.dto.response;

import com.app.authservice.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profileImageUrl;
    private Boolean emailVerified;
    private String provider;
    private LocalDateTime createdAt;

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setProvider(user.getProvider().name());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}

