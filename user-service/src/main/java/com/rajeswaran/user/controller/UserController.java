package com.rajeswaran.user.controller;

import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.user.entity.User;
import com.rajeswaran.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
    @GetMapping
    public List<User> getAllUsers() {
        log.info("Received request: getAllUsers");
        List<User> users = userService.getAllUsers();
        log.info("Completed request: getAllUsers, count={}", users.size());
        return users;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();

        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in database"));
    }

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN) or hasRole(T(com.rajeswaran.common.AppConstants).ROLE_ACCOUNT_HOLDER)")
    @PostMapping
    public User createUser() {
        log.info("Received request: createUser from JWT");
        String username = SecurityUtil.getCurrentUsername();
        String email = null;
        String fullName = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            email = jwt.getClaimAsString("email");
            fullName = jwt.getClaimAsString("name");
        }
        User created = userService.createUserFromJwt(username, email, fullName);
        log.info("Completed request: createUser, createdId={}", created.getUserId());
        return created;
    }
}

