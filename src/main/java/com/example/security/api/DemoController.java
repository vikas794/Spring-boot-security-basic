package com.example.security.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint. Anyone can access it.";
    }

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is a protected endpoint. Any authenticated user can access it.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "This is an admin endpoint. Only ADMINs can access it.";
    }

    @GetMapping("/resource/{id}")
    @PreAuthorize("@abac.canAccess(#id, authentication)")
    public String abacEndpoint(@PathVariable Long id) {
        return "You have access to resource: " + id;
    }
}
