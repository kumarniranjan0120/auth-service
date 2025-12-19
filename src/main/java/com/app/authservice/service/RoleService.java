package com.app.authservice.service;


import com.app.authservice.model.Role;
import com.app.authservice.repo.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        List<String> roleNames = Arrays.asList("USER", "ADMIN", "MODERATOR");

        for (String roleName : roleNames) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName + " role")
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
    }

    public Role getUserRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
    }

    public Role getAdminRole() {
        return roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
    }

    public Role getModeratorRole() {
        return roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new RuntimeException("MODERATOR role not found"));
    }
}
