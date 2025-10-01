package com.example.coffeeshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow requests from any origin during development.
        // In production, replace "*" with your actual frontend domain (e.g., "https://mycoffeeshop.com").
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080", "null") // 'null' is often needed when running from a local file://
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
                // IMPORTANT: You need to allow the custom headers you are using for rate limiting
                .allowedHeaders("Content-Type", "X-Customer-Id", "X-Customer-Type")
                .allowCredentials(true);
    }
}