package com.example.coffeeshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply CORS to all /api endpoints
                .allowedOrigins(
                        "http://127.0.0.1:5500", // Your local frontend URL
                        "http://localhost:5500" // Another common local URL
                        // For a public demo, you could use "*" but it's less secure
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-Customer-Id", "X-Customer-Type") // IMPORTANT: Allow your custom headers
                .allowCredentials(false)
                .maxAge(3600);
    }
}