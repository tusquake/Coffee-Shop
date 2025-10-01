package com.example.coffeeshop.interceptor;

import com.example.coffeeshop.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that checks rate limits before processing requests
 *
 * Real-world analogy:
 * Like a bouncer at a club who checks if a person
 * has already entered too many times in a short period
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Extract customer identifier (could be user ID from JWT in real app)
        String customerId = request.getHeader("X-Customer-Id");
        String customerType = request.getHeader("X-Customer-Type");

        // Default to IP address if no customer ID provided
        if (customerId == null || customerId.isEmpty()) {
            customerId = getClientIP(request);
            customerType = "GUEST"; // Anonymous users are guests
        }

        // Default customer type
        if (customerType == null || customerType.isEmpty()) {
            customerType = "STANDARD";
        }

        log.info("Checking rate limit for customer: {} (type: {})", customerId, customerType);

        // Check if request is allowed
        if (rateLimiterService.allowRequest(customerId, customerType)) {
            long remaining = rateLimiterService.getRemainingTokens(customerId, customerType);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            return true; // Allow request to proceed
        } else {
            // Rate limit exceeded
            long waitTime = rateLimiterService.getSecondsUntilRefill(customerId, customerType);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(waitTime));
            response.getWriter().write(
                    String.format("Rate limit exceeded. Please try again in %d seconds.", waitTime)
            );

            log.warn("Rate limit exceeded for customer: {}", customerId);
            return false; // Block request
        }
    }

    /**
     * Extract client IP address from request
     * Handles proxies and load balancers
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}