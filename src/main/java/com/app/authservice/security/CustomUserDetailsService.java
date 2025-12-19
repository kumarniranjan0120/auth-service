package com.app.authservice.security;

import com.app.authservice.model.User;
import com.app.authservice.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Let people login with either username or email
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)
                );

        validateUserAccount(user);

        log.debug("Loaded user by username/email: {}", usernameOrEmail);
        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with id: " + id)
                );

        validateUserAccount(user);

        log.debug("Loaded user by id: {}", id);
        return UserPrincipal.create(user);
    }

    private void validateUserAccount(User user) {
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
            throw new UsernameNotFoundException("User account is locked");
        }

        // Check if account is expired (you can add this field to User entity)
        // if (user.getAccountExpired()) {
        //     throw new UsernameNotFoundException("User account has expired");
        // }

        // Check if credentials are expired (you can add this field to User entity)
        // if (user.getCredentialsExpired()) {
        //     throw new UsernameNotFoundException("User credentials have expired");
        // }
    }

    @Transactional
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        validateUserAccount(user);

        log.debug("Loaded user by email: {}", email);
        return UserPrincipal.create(user);
    }
}