package com.marketplace.notificationservice.service;

import com.marketplace.notificationservice.event.OrderEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from}")
    private String fromEmail;

    public void sendOrderConfirmation(OrderEvent event) {
        Context context = new Context();
        context.setVariable("orderId", event.getOrderId());
        context.setVariable("totalAmount", event.getTotalAmount());
        context.setVariable("itemCount", event.getItemCount());
        context.setVariable("status", event.getStatus());

        String htmlContent = templateEngine.process("order-confirmation", context);

        sendHtmlEmail(
                "buyer" + event.getBuyerId() + "@marketplace.com",
                "Order #" + event.getOrderId() + " — Confirmation",
                htmlContent
        );
    }

    public void sendStatusUpdate(OrderEvent event) {
        Context context = new Context();
        context.setVariable("orderId", event.getOrderId());
        context.setVariable("status", event.getStatus());

        String htmlContent = templateEngine.process("order-status-update", context);

        sendHtmlEmail(
                "buyer" + event.getBuyerId() + "@marketplace.com",
                "Order #" + event.getOrderId() + " — Status Update",
                htmlContent
        );
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
