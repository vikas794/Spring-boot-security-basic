package com.example.security;

import com.example.security.auth.jwt.JwtAuthenticationFilter;
import com.example.security.auth.jwt.JwtService;
import com.example.security.auth.jwt.TokenRevocationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.FilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class JwtFilterExceptionTest {

    @Test
    public void testInvalidJwtReturns401() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        TokenRevocationService tokenRevocationService = mock(TokenRevocationService.class);

        when(jwtService.extractUsername(anyString())).thenThrow(new JwtException("Invalid token"));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRevocationService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }
}
