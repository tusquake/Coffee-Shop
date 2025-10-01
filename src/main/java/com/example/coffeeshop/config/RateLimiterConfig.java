package com.example.coffeeshop.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for Rate Limiting
 *
 * Real-world analogy:
 * - Like a coffee shop limiting how many orders one customer can place
 * - Prevents overwhelming the baristas (your API servers)
 * - Ensures fair access for all customers
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Cache for storing rate limiter buckets per customer
     * Think of this as a registry tracking each customer's order count
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("rateLimitBuckets");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(1, TimeUnit.HOURS));
        return cacheManager;
    }

    /**
     * Map to store buckets for each user/IP
     * ConcurrentHashMap ensures thread-safety for multiple simultaneous requests
     */
    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimiterBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a standard bucket for regular customers
     *
     * Rules: 5 orders per minute
     * Analogy: A regular customer can order 5 coffees per minute
     */
    public static Bucket createStandardBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a premium bucket for VIP customers
     *
     * Rules: 20 orders per minute
     * Analogy: VIP members get priority and can order more frequently
     */
    public static Bucket createPremiumBucket() {
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a strict bucket for anonymous users
     *
     * Rules: 2 orders per minute
     * Analogy: Guests without membership have stricter limits
     */
    public static Bucket createGuestBucket() {
        Bandwidth limit = Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}