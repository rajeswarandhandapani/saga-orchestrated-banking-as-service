package com.rajeswaran.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.user.dto.UpdateUserRequest;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole(T(com.rajeswaran.common.AppConstants).ROLE_BAAS_ADMIN)")
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        log.info("Received request: updateUser id={}, fullName={}, email={}", id, request.getFullName(), request.getEmail());
        return userService.updateUserDetails(id, request.getFullName(), request.getEmail());
    }

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
}

