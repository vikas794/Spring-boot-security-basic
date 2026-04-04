package com.example.security.api;

import com.example.security.config.SecurityProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final Cache<String, Bucket> cache;

    public RateLimitFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.cache = Caffeine.newBuilder()
                .maximumSize(10000) // Bound the cache size to prevent OOM
                .expireAfterAccess(Duration.ofHours(1))
                .build();
    }

    private Bucket createNewBucket() {
        SecurityProperties.RateLimit rlProps = securityProperties.getRateLimit();
        long capacity = rlProps.getCapacity();
        Refill refill = Refill.greedy(rlProps.getRefillTokens(), Duration.ofSeconds(rlProps.getRefillDurationSeconds()));
        Bandwidth limit = Bandwidth.builder().capacity(capacity).refillGreedy(rlProps.getRefillTokens(), Duration.ofSeconds(rlProps.getRefillDurationSeconds())).build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String ip) {
        return cache.get(ip, k -> createNewBucket());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!securityProperties.getRateLimit().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        Bucket bucket = resolveBucket(clientIp);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");
        }
    }
}
