package com.example.coffeeshop.controller;

import com.example.coffeeshop.model.CoffeeOrder;
import com.example.coffeeshop.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Coffee Shop REST API Controller
 *
 * Real-world scenario:
 * - Customers can place coffee orders via API
 * - Rate limiting prevents abuse (e.g., bots placing 1000s of orders)
 * - Different customer tiers have different limits
 */
@RestController
@RequestMapping("/api/coffee")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500", "null"},
        allowedHeaders = {"Content-Type", "X-Customer-Id", "X-Customer-Type"})
public class CoffeeShopController {

    private final RateLimiterService rateLimiterService;
    private final Map<String, List<CoffeeOrder>> orderHistory = new HashMap<>();

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(
            @RequestHeader(value = "X-Customer-Id", defaultValue = "anonymous") String customerId,
            @RequestBody Map<String, Object> orderRequest) {

        String coffeeType = (String) orderRequest.get("coffeeType");
        String size = (String) orderRequest.getOrDefault("size", "MEDIUM");
        int quantity = (int) orderRequest.getOrDefault("quantity", 1);

        double price = calculatePrice(coffeeType, size, quantity);


        CoffeeOrder order = new CoffeeOrder(
                UUID.randomUUID().toString(),
                customerId,
                coffeeType,
                size,
                quantity,
                price,
                LocalDateTime.now(),
                "CONFIRMED"
        );

        // Store order
        orderHistory.computeIfAbsent(customerId, k -> new ArrayList<>()).add(order);

        log.info("Order placed successfully: {}", order.getOrderId());

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("message", "Order placed successfully!");
        response.put("totalPrice", price);
        response.put("estimatedTime", "5-10 minutes");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestHeader(value = "X-Customer-Id", defaultValue = "anonymous") String customerId) {

        List<CoffeeOrder> orders = orderHistory.getOrDefault(customerId, new ArrayList<>());

        Map<String, Object> response = new HashMap<>();
        response.put("customerId", customerId);
        response.put("totalOrders", orders.size());
        response.put("orders", orders);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/rate-limit-status")
    public ResponseEntity<?> getRateLimitStatus(
            @RequestHeader(value = "X-Customer-Id", defaultValue = "anonymous") String customerId,
            @RequestHeader(value = "X-Customer-Type", defaultValue = "STANDARD") String customerType) {

        long remaining = rateLimiterService.getRemainingTokens(customerId, customerType);

        Map<String, Object> response = new HashMap<>();
        response.put("customerId", customerId);
        response.put("customerType", customerType);
        response.put("remainingRequests", remaining);
        response.put("message", String.format("You can place %d more orders in the current window", remaining));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/menu")
    public ResponseEntity<?> getMenu() {
        List<Map<String, Object>> menu = Arrays.asList(
                Map.of("name", "Espresso", "sizes", Arrays.asList("SMALL", "MEDIUM"),
                        "basePrice", 3.50),
                Map.of("name", "Cappuccino", "sizes", Arrays.asList("SMALL", "MEDIUM", "LARGE"),
                        "basePrice", 4.50),
                Map.of("name", "Latte", "sizes", Arrays.asList("SMALL", "MEDIUM", "LARGE"),
                        "basePrice", 4.75),
                Map.of("name", "Americano", "sizes", Arrays.asList("SMALL", "MEDIUM", "LARGE"),
                        "basePrice", 3.75),
                Map.of("name", "Mocha", "sizes", Arrays.asList("MEDIUM", "LARGE"),
                        "basePrice", 5.25)
        );

        return ResponseEntity.ok(Map.of("menu", menu));
    }

    private double calculatePrice(String coffeeType, String size, int quantity) {
        Map<String, Double> basePrices = Map.of(
                "Espresso", 3.50,
                "Cappuccino", 4.50,
                "Latte", 4.75,
                "Americano", 3.75,
                "Mocha", 5.25
        );

        double basePrice = basePrices.getOrDefault(coffeeType, 4.00);

        // Size multiplier
        double sizeMultiplier = switch (size.toUpperCase()) {
            case "SMALL" -> 0.8;
            case "LARGE" -> 1.3;
            default -> 1.0; // MEDIUM
        };

        return Math.round(basePrice * sizeMultiplier * quantity * 100.0) / 100.0;
    }
}