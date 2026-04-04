package com.example.security.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuditListener.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        log.info("Audit Log: Authentication Success for user: {}", event.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        log.warn("Audit Log: Authentication Failure for user: {} due to {}",
                event.getAuthentication().getName(),
                event.getException().getMessage());
    }
}
