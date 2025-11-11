package com.miniecommerce.notificationservice.kafka;

import com.miniecommerce.notificationservice.event.UserEvent;
import com.miniecommerce.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "user-events",
            groupId = "notification-service-group",
            containerFactory = "userEventKafkaListenerContainerFactory"
    )
    public void consumeUserEvent(UserEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("👤 Received User Event: {}", event.getEventType());

            switch (event.getEventType()) {
                case "USER_CREATED":
                    handleUserCreated(event);
                    break;
                case "USER_UPDATED":
                    handleUserUpdated(event);
                    break;
                default:
                    log.warn("Unknown user event type: {}", event.getEventType());
            }

            acknowledgment.acknowledge();
            log.info("✅ User event processed successfully: {}", event.getEventId());

        } catch (Exception e) {
            log.error("❌ Error processing user event: {}", e.getMessage(), e);
        }
    }

    private void handleUserCreated(UserEvent event) {
        log.info("👋 NOTIFICATION: Welcome new user!");
        log.info("   Name: {}", event.getUserName());
        log.info("   Email: {}", event.getEmail());
        sendWelcomeEmail(event);
    }

    private void handleUserUpdated(UserEvent event) {
        log.info("✏️ NOTIFICATION: User profile updated");
        log.info("   Name: {}", event.getUserName());
        log.info("   Email: {}", event.getEmail());
    }

    private void sendWelcomeEmail(UserEvent event) {
        log.info("📧 Sending welcome email to: {}", event.getEmail());

        emailService.sendWelcomeEmail(event.getEmail(), event.getUserName());

    }
}