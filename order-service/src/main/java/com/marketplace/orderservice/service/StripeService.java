package com.marketplace.orderservice.service;

import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.entity.OrderItem;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    public Session createCheckoutSession(Order order) throws StripeException {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?orderId=" + order.getId())
                .setCancelUrl(cancelUrl + "?orderId=" + order.getId())
                .putMetadata("orderId", order.getId().toString());

        for (OrderItem item : order.getItems()) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount(item.getPrice()
                                                    .multiply(java.math.BigDecimal.valueOf(100))
                                                    .longValueExact())
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getProductName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }


        Session session = Session.create(paramsBuilder.build());
        log.info("Stripe session created: {} for order: {}", session.getId(), order.getId());
        return session;
    }
}