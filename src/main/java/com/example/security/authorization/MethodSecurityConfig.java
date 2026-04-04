package com.example.security.authorization;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize, @PostAuthorize
public class MethodSecurityConfig {
    // Basic method security enablement.
    // Further configuration can be added here if needed, for instance customizing expression handlers.
}
