package com.example.security.auth;

import com.example.security.auth.jwt.JwtService;
import com.example.security.auth.jwt.TokenRevocationService;
import com.example.security.core.AuthResponse;
import com.example.security.core.LoginRequest;
import com.example.security.core.Role;
import com.example.security.core.User;
import com.example.security.core.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRevocationService tokenRevocationService;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, TokenRevocationService tokenRevocationService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostConstruct
    public void init() {
        // Initialize dummy users for testing
        if (userRepository.count() == 0) {
            userRepository.saveAll(java.util.List.of(
                new User("user", passwordEncoder.encode("password"), Role.USER),
                new User("admin", passwordEncoder.encode("password"), Role.ADMIN)
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        String username = request.getUsername();
        int attempts = loginAttempts.getOrDefault(username, 0);
        if (attempts >= MAX_ATTEMPTS) {
            throw new LockedException("Account is locked due to too many failed attempts.");
        }

        // CAPTCHA Placeholder: in production, validate a captcha token here
        // if (!captchaService.isValid(request.getCaptchaToken())) { ... }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Reset attempts on success
            loginAttempts.remove(username);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            // Refresh token could also be generated here

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (Exception e) {
            loginAttempts.put(username, attempts + 1);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            tokenRevocationService.revokeToken(jwt);
            return ResponseEntity.ok("Successfully logged out. Token revoked.");
        }
        return ResponseEntity.badRequest().body("No token provided.");
    }
}
