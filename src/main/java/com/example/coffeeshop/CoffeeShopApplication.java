package com.example.coffeeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Coffee Shop Rate Limiter Demo Application
 *
 * This application demonstrates rate limiting in Spring Boot using a coffee shop analogy:
 *
 * REAL-WORLD SCENARIOS:
 * 1. Prevent abuse: Stop bots from placing thousands of fake orders
 * 2. Fair usage: Ensure all customers get a chance to order
 * 3. Resource protection: Prevent overwhelming your servers/baristas
 * 4. Tiered service: VIP customers get higher limits
 *
 * RATE LIMIT TIERS:
 * - GUEST: 2 orders per minute (anonymous users)
 * - STANDARD: 5 orders per minute (regular customers)
 * - PREMIUM: 20 orders per minute (VIP members)
 *
 * HOW TO TEST:
 * 1. Start the application
 * 2. Use curl or Postman to make requests
 * 3. Try exceeding the limits to see rate limiting in action
 */
@SpringBootApplication
public class CoffeeShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeShopApplication.class, args);
	}
}