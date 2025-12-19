package com.app.authservice.security;


import com.app.authservice.model.User;
import com.app.authservice.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, attributes);

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> registerNewUser(oAuth2UserInfo, registrationId));

        updateExistingUser(user, oAuth2UserInfo);

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName())));
        });

        return UserPrincipal.create(user, attributes, authorities);
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, String provider) {
        User user = new User();
        user.setProvider(User.AuthProvider.valueOf(provider.toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setEmailVerified(true);
        user.setUsername(generateUsername(oAuth2UserInfo));
        user.setFirstName(oAuth2UserInfo.getFirstName());
        user.setLastName(oAuth2UserInfo.getLastName());
        user.setProfileImageUrl(oAuth2UserInfo.getImageUrl());

        // Set default role (USER)
        // You can fetch default role from database here

        return userRepository.save(user);
    }

    private void updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        if (!user.getProvider().equals(User.AuthProvider.LOCAL)) {
            user.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
            userRepository.save(user);
        }
    }

    private String generateUsername(OAuth2UserInfo oAuth2UserInfo) {
        String baseUsername = oAuth2UserInfo.getEmail().split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}