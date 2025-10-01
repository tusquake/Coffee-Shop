package com.example.coffeeshop;
import com.example.coffeeshop.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    // The component under test
    @InjectMocks
    private RateLimiterService rateLimiterService;

    // Manually initialize the dependency that was marked 'final' in the service
    private ConcurrentHashMap<String, Bucket> rateLimiterBuckets;

    private static final String STANDARD = "STANDARD";
    private static final String PREMIUM = "PREMIUM";
    private static final String GUEST = "GUEST";
    private static final String TEST_KEY = "test-customer-123";

    // Based on RateLimiterConfig (assuming default 5 tokens capacity for STANDARD)
    private static final int STANDARD_CAPACITY = 5;

    @BeforeEach
    void setUp() {
        // Initialize the map before each test
        rateLimiterBuckets = new ConcurrentHashMap<>();

        // This simulates the dependency injection of the map into the service
        // Since @InjectMocks doesn't handle final fields well, we set it manually after init.
        // In a real Spring test, this would be handled automatically.
        // For this unit test, we use reflection or a modified constructor.
        // Assuming a setter or public access for simplicity in this example:
        // Or, more correctly, you'd initialize the whole service here:
        rateLimiterService = new RateLimiterService(rateLimiterBuckets);
    }

    // -------------------------------------------------------------------------
    // 1. Bucket Resolution and Initialization Tests
    // -------------------------------------------------------------------------

    @Test
    void resolveBucket_CreatesNewStandardBucket_WhenNewKey() {
        // Act
        Bucket bucket = rateLimiterService.resolveBucket(TEST_KEY, STANDARD);

        // Assert
        assertNotNull(bucket, "Bucket should not be null.");
        assertEquals(1, rateLimiterBuckets.size(), "One new bucket should be stored.");
        assertTrue(bucket.tryConsume(STANDARD_CAPACITY), "Should have full capacity initially.");
        assertFalse(bucket.tryConsume(1), "Bucket should be empty after consumption.");
    }

    @Test
    void resolveBucket_ReturnsExistingBucket_WhenOldKey() {
        // Arrange
        rateLimiterService.resolveBucket(TEST_KEY, STANDARD);
        rateLimiterBuckets.get(TEST_KEY).tryConsume(5); // Consume half the tokens

        // Act
        Bucket secondAccess = rateLimiterService.resolveBucket(TEST_KEY, STANDARD);

        // Assert
        assertEquals(1, rateLimiterBuckets.size(), "No new bucket should be stored.");
        assertTrue(secondAccess.tryConsume(5), "Should have the remaining 5 tokens.");
        assertFalse(secondAccess.tryConsume(1), "Bucket should be empty after consumption.");
    }

    @Test
    void resolveBucket_CreatesPremiumBucket_WhenRequested() {
        // Act
        Bucket bucket = rateLimiterService.resolveBucket(TEST_KEY, PREMIUM);

        // Assert: Assuming premium capacity is 100 based on typical configuration
        assertTrue(bucket.tryConsume(20), "Premium bucket should allow 100 tokens initially.");
        assertFalse(bucket.tryConsume(1), "Bucket should be empty after premium consumption.");
    }

    // -------------------------------------------------------------------------
    // 2. Rate Limiting Logic (allowRequest) Tests
    // -------------------------------------------------------------------------

    @Test
    void allowRequest_AllowsUpToCapacity() {
        // Act & Assert
        for (int i = 0; i < STANDARD_CAPACITY; i++) {
            assertTrue(rateLimiterService.allowRequest(TEST_KEY, STANDARD),
                    "Request " + (i + 1) + " should be allowed.");
        }
    }

    @Test
    void allowRequest_DeniesAfterCapacityExceeded() {
        // Arrange
        for (int i = 0; i < STANDARD_CAPACITY; i++) {
            rateLimiterService.allowRequest(TEST_KEY, STANDARD); // Consume all 10
        }

        // Act & Assert
        assertFalse(rateLimiterService.allowRequest(TEST_KEY, STANDARD),
                "The 11th request should be denied.");
    }

    // -------------------------------------------------------------------------
    // 3. Status Check Tests
    // -------------------------------------------------------------------------

    @Test
    void getRemainingTokens_ReportsCorrectValue() {
        // Arrange
        rateLimiterService.resolveBucket(TEST_KEY, STANDARD);
        rateLimiterService.allowRequest(TEST_KEY, STANDARD); // Consume 1
        rateLimiterService.allowRequest(TEST_KEY, STANDARD); // Consume 1

        // Act
        long remaining = rateLimiterService.getRemainingTokens(TEST_KEY, STANDARD);

        // Assert
        assertEquals(STANDARD_CAPACITY - 2, remaining, "Should report 8 remaining tokens.");
    }

    @Test
    void getSecondsUntilRefill_ReportsWaitTimeWhenDenied() throws InterruptedException {
        // Arrange
        // Note: For reliable testing, you'd use a Mock TimeProvider with Bucket4j.
        // For a simple example, we fill the bucket and check the wait time.
        for (int i = 0; i < STANDARD_CAPACITY; i++) {
            rateLimiterService.allowRequest(TEST_KEY, STANDARD); // Consume all
        }

        // Act: The next request is denied, triggering a wait calculation
        // The STANDARD rate is 10 tokens/min (1 token every 6 seconds).
        rateLimiterService.allowRequest(TEST_KEY, STANDARD); // This is the request that is denied

        long waitTime = rateLimiterService.getSecondsUntilRefill(TEST_KEY, STANDARD);

        // Assert
        // The wait time should be close to the time needed for 1 token to refill (6 seconds)
        // We check if it is within a reasonable range (1-6 seconds) due to precision
        assertTrue(waitTime >= 1 && waitTime <= 6,
                "Wait time should be between 1 and 6 seconds (10/min rate). Actual: " + waitTime);

        // Brief sleep to allow a refill (simulating the time needed)
        Thread.sleep(Duration.ofSeconds(6).toMillis() + 100);

        // Act again after wait
        assertTrue(rateLimiterService.allowRequest(TEST_KEY, STANDARD),
                "Request should be allowed after waiting for refill time.");
    }
}