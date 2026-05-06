package com.marketplace.orderservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StripeConfig {


    @Value("${stripe.api-key}")
    private String apiKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = apiKey;
        log.info("Stripe initialized (key prefix: {})",
                apiKey == null || apiKey.length() < 7 ? "EMPTY" : apiKey.substring(0, 7));
    }
}
