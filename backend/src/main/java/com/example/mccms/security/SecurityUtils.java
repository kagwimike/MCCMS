package com.example.mccms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for common security operations.
 */
@Component
public class SecurityUtils {

    /**
     * Retrieves the email of the currently authenticated user.
     * @throws RuntimeException if no authentication is found.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        return auth.getName();
    }
}
