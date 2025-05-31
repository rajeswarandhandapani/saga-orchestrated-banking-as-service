package com.rajeswaran.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    private SecurityUtil() {
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            String username = jwt.getClaimAsString("preferred_username");
            if (username == null) {
                throw new RuntimeException("User not found in token");
            }
            return username;
        }
        throw new RuntimeException("JWT principal not found");
    }
}
