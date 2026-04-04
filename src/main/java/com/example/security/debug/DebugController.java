package com.example.security.debug;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/security-context")
    public Map<String, Object> getSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> contextInfo = new HashMap<>();
        if (authentication == null) {
            contextInfo.put("status", "No authentication found in context.");
            return contextInfo;
        }

        contextInfo.put("principal", authentication.getPrincipal());
        contextInfo.put("authorities", authentication.getAuthorities());
        contextInfo.put("isAuthenticated", authentication.isAuthenticated());
        contextInfo.put("details", authentication.getDetails());

        return contextInfo;
    }
}
