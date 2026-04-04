package com.example.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Custom bean to demonstrate Attribute-Based Access Control (ABAC).
 * Instead of checking a role, we check attributes related to the user and the resource.
 * Example usage: @PreAuthorize("@abac.canAccess(#resourceId, authentication)")
 */
@Component("abac")
public class AbacPermissionEvaluator {

    public boolean canAccess(Long resourceId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();

        // --- Simulated ABAC Logic ---
        // In a real application, you would fetch the resource from the DB and verify
        // if the current user (username) is the owner, belongs to the same department, etc.

        // For demonstration: user 'admin' can access anything, other users can only access resourceId <= 10.
        if ("admin".equals(username)) {
            return true;
        }

        return resourceId != null && resourceId <= 10;
    }
}
