package com.example.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Coffee Order model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeOrder {
    private String orderId;
    private String customerId;
    private String coffeeType;
    private String size;
    private int quantity;
    private double price;
    private LocalDateTime orderTime;
    private String status;
}

/**
 * Order request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderRequest {
    private String coffeeType;
    private String size;
    private int quantity;
}

/**
 * Order response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderResponse {
    private String orderId;
    private String message;
    private double totalPrice;
    private String estimatedTime;
}