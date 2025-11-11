package com.miniecommerce.notificationservice.kafka;

import com.miniecommerce.notificationservice.event.ProductEvent;
import com.miniecommerce.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "product-events",
            groupId = "notification-service-group",
            containerFactory = "productEventKafkaListenerContainerFactory"
    )
    public void consumeProductEvent(ProductEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("📦 Received Product Event: {}", event.getEventType());

            switch (event.getEventType()) {
                case "PRODUCT_CREATED":
                    handleProductCreated(event);
                    break;
                case "STOCK_LOW":
                    handleStockLow(event);
                    break;
                case "STOCK_OUT":
                    handleStockOut(event);
                    break;
                default:
                    log.warn("Unknown product event type: {}", event.getEventType());
            }

            acknowledgment.acknowledge();
            log.info("✅ Product event processed successfully: {}", event.getEventId());

        } catch (Exception e) {
            log.error("❌ Error processing product event: {}", e.getMessage(), e);
        }
    }

    private void handleProductCreated(ProductEvent event) {
        log.info("🆕 NOTIFICATION: New product added to catalog!");
        log.info("   Product: {}", event.getProductName());
        log.info("   Price: ${}", event.getPrice());
        log.info("   Stock: {}", event.getStock());
    }

    private void handleStockLow(ProductEvent event) {
        log.warn("⚠️ NOTIFICATION: LOW STOCK ALERT!");
        log.warn("   Product: {}", event.getProductName());
        log.warn("   Remaining stock: {}", event.getStock());

        emailService.sendLowStockAlert(event.getProductName(), event.getProductId(), event.getStock());

        sendAlert(
                "Procurement Team",
                "Low Stock Alert",
                String.format("Product '%s' is running low! Only %d units remaining.",
                        event.getProductName(), event.getStock())
        );
    }

    private void handleStockOut(ProductEvent event) {
        log.error("🚨 NOTIFICATION: OUT OF STOCK!");
        log.error("   Product: {}", event.getProductName());

        emailService.sendLowStockAlert(event.getProductName(), event.getProductId(), event.getStock());

        sendAlert(
                "Procurement Team",
                "OUT OF STOCK - URGENT",
                String.format("Product '%s' is OUT OF STOCK! Immediate restocking required.",
                        event.getProductName())
        );
    }

    private void sendAlert(String recipient, String subject, String message) {
        log.info("🚨 Sending alert to: {}", recipient);
        log.info("   Subject: {}", subject);
        log.info("   Message: {}", message);
    }
}