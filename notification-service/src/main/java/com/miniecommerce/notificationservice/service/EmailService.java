package com.miniecommerce.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.enabled}")
    private boolean emailEnabled;

    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.info("📧 Email sending is disabled. Would send to: {} with subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Process template with variables
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {} - Error: {}", to, e.getMessage(), e);
        }
    }

    public void sendOrderConfirmationEmail(String to, String customerName, Long orderId,
                                           String productName, Integer quantity,
                                           String totalPrice, String orderDate) {
        Map<String, Object> variables = Map.of(
                "customerName", customerName,
                "orderId", orderId,
                "productName", productName,
                "quantity", quantity,
                "totalPrice", totalPrice,
                "orderDate", orderDate
        );

        sendHtmlEmail(to, "Order Confirmation - Order #" + orderId, "order-confirmation", variables);
    }

    public void sendOrderShippedEmail(String to, String customerName, Long orderId,
                                      String estimatedDelivery, String trackingNumber) {
        Map<String, Object> variables = Map.of(
                "customerName", customerName,
                "orderId", orderId,
                "estimatedDelivery", estimatedDelivery,
                "trackingNumber", trackingNumber != null ? trackingNumber : "N/A"
        );

        sendHtmlEmail(to, "Your Order Has Shipped! - Order #" + orderId, "order-shipped", variables);
    }

    public void sendOrderFailedEmail(String customerName, Long orderId) {
        Map<String, Object> variables = Map.of(
                "customerName", customerName,
                "orderId", orderId
        );

        sendHtmlEmail(fromEmail, "Order Failed - Order #" + orderId, "order-failed", variables);
    }

    public void sendWelcomeEmail(String to, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "email", to
        );

        sendHtmlEmail(to, "Welcome to E-Commerce Platform! 🎉", "welcome", variables);
    }

    public void sendLowStockAlert(String productName, Long productId, Integer stock) {
        Map<String, Object> variables = Map.of(
                "productName", productName,
                "productId", productId,
                "stock", stock
        );

        sendHtmlEmail(fromEmail, "⚠️ Low Stock Alert: " + productName, "low-stock-alert", variables);
    }
}