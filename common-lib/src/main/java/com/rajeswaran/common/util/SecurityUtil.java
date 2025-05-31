package com.rajeswaran.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Extracts roles from the JWT token in the current security context
     *
     * @return Set of role strings, prefixed with "ROLE_" if not already
     */
    public static Set<String> extractRolesFromJwt() {
        Set<String> roles = new HashSet<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // Get roles from JWT claims - format may vary depending on your Keycloak configuration
            // Typically roles are in realm_access.roles or resource_access.[client-id].roles
            try {
                @SuppressWarnings("unchecked")
                var realmAccess = (java.util.Map<String, Object>) jwt.getClaim("realm_access");
                if (realmAccess != null) {
                    @SuppressWarnings("unchecked")
                    var realmRoles = (List<String>) realmAccess.get("roles");
                    if (realmRoles != null) {
                        // Convert to ROLE_ format if needed
                        roles.addAll(realmRoles.stream()
                                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                                .collect(Collectors.toSet()));
                    }
                }

                // You might also need to check resource_access roles depending on your configuration
            } catch (Exception e) {
                // Log error but continue processing
                System.err.println("Error extracting roles from JWT: " + e.getMessage());
            }
        }
        return roles;
    }
}
