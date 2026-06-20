package com.example.security.config;

import com.example.security.auth.jwt.JwtAuthenticationFilter;
import com.example.security.auth.jwt.JwtService;
import com.example.security.auth.jwt.TokenRevocationService;
import com.example.security.api.RateLimitFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "security", name = "auth", havingValue = "jwt", matchIfMissing = true)
public class JwtSecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;
    private final TokenRevocationService tokenRevocationService;

    public JwtSecurityConfig(AuthenticationProvider authenticationProvider, JwtService jwtService, UserDetailsService userDetailsService, SecurityProperties securityProperties, TokenRevocationService tokenRevocationService) {
        this.authenticationProvider = authenticationProvider;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.securityProperties = securityProperties;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Bean
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRevocationService);
        RateLimitFilter rateLimitFilter = new RateLimitFilter(securityProperties);

        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF disabled for stateless JWT
            .cors(cors -> cors.configure(http))    // Optional CORS config
            .authorizeHttpRequests(auth -> auth
                // WHY: Explicitly restrict access to sensitive admin endpoints like debug and database console.
                .requestMatchers("/h2-console/**", "/debug/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/**", "/error", "/api/demo/public").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class) // Apply rate limiting first
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Application Hardening: Security Headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // Only for H2 console, otherwise use SAMEORIGIN
                .xssProtection(xss -> xss.disable()) // Modern browsers use CSP instead
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none';"))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            );

        return http.build();
    }
}
