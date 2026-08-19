package com.nurseadda.project.config;

import com.razorpay.RazorpayClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
                log.warn("Razorpay credentials not configured - payment gateway disabled");
                return null;
            }
            return new RazorpayClient(keyId, keySecret);
        } catch (Exception e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            return null;
        }
    }
}
