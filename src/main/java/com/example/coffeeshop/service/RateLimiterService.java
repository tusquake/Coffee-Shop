package com.example.coffeeshop.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

import static com.example.coffeeshop.config.RateLimiterConfig.*;

/**
 * Service to handle rate limiting logic
 *
 * Think of this as the manager who checks if a customer
 * has exceeded their order limit before accepting new orders
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> rateLimiterBuckets;

    /**
     * Resolve bucket based on customer type
     *
     * @param key - unique identifier (customer ID or IP address)
     * @param customerType - STANDARD, PREMIUM, or GUEST
     * @return Bucket for the customer
     */
    public Bucket resolveBucket(String key, String customerType) {
        return rateLimiterBuckets.computeIfAbsent(key, k -> {
            log.info("Creating new bucket for key: {} with type: {}", key, customerType);
            return switch (customerType.toUpperCase()) {
                case "PREMIUM" -> createPremiumBucket();
                case "GUEST" -> createGuestBucket();
                default -> createStandardBucket();
            };
        });
    }

    /**
     * Check if request is allowed and consume a token
     *
     * @param key - unique identifier
     * @param customerType - customer tier
     * @return true if request allowed, false otherwise
     */
    public boolean allowRequest(String key, String customerType) {
        Bucket bucket = resolveBucket(key, customerType);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.info("Request allowed for key: {}. Remaining tokens: {}",
                    key, probe.getRemainingTokens());
            return true;
        } else {
            log.warn("Request denied for key: {}. Please wait {} seconds",
                    key, probe.getNanosToWaitForRefill() / 1_000_000_000);
            return false;
        }
    }

    /**
     * Get remaining tokens for a customer
     * Useful for showing customers how many orders they can still place
     */
    public long getRemainingTokens(String key, String customerType) {
        Bucket bucket = resolveBucket(key, customerType);
        return bucket.getAvailableTokens();
    }

    /**
     * Get time until next token refill
     * Tells customer when they can order again
     */
    public long getSecondsUntilRefill(String key, String customerType) {
        Bucket bucket = resolveBucket(key, customerType);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            return probe.getNanosToWaitForRefill() / 1_000_000_000;
        }

        // Restore the token we just consumed for checking
        return 0;
    }
}