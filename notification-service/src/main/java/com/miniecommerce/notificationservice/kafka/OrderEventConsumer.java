package com.miniecommerce.notificationservice.kafka;

import com.miniecommerce.notificationservice.event.OrderEvent;
import com.miniecommerce.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group",
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(OrderEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("📦 Received Order Event: {}", event.getEventType());

            switch (event.getEventType()) {
                case "ORDER_CREATED":
                    handleOrderCreated(event);
                    break;
                case "ORDER_COMPLETED":
                    handleOrderCompleted(event);
                    break;
                case "ORDER_FAILED":
                    handleOrderFailed(event);
                    break;
                default:
                    log.warn("Unknown order event type: {}", event.getEventType());
            }

            acknowledgment.acknowledge();
            log.info("✅ Order event processed successfully: {}", event.getEventId());

        } catch (Exception e) {
            log.error("❌ Error processing order event: {}", e.getMessage(), e);
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        log.info("🔔 Processing ORDER_CREATED event for order #{}", event.getOrderId());

        // Format date
        String orderDate = event.getTimestamp()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

        String customerEmail = event.getUserEmail();

        emailService.sendOrderConfirmationEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderId(),
                event.getProductName(),
                event.getQuantity(),
                String.format("%.2f", event.getTotalPrice()),
                orderDate
        );

        log.info("📧 Order confirmation email sent to: {}", customerEmail);
    }

    private void handleOrderCompleted(OrderEvent event) {
        log.info("✅ Processing ORDER_COMPLETED event for order #{}", event.getOrderId());

        String customerEmail = event.getUserEmail();
        String trackingNumber = "TRK" + event.getOrderId() + System.currentTimeMillis();

        emailService.sendOrderShippedEmail(
                event.getUserEmail(),
                event.getUserName(),
                event.getOrderId(),
                "3-5 business days",
                trackingNumber
        );

        log.info("📧 Order shipped email sent to: {}", customerEmail);
    }



    private void handleOrderFailed(OrderEvent event) {
        log.error("❌ Processing ORDER_FAILED event for order #{}", event.getOrderId());
        log.info("🚨 Sending failure alert to admin");

        emailService.sendOrderFailedEmail(
                event.getUserEmail(),
                event.getOrderId()
        );
    }
}