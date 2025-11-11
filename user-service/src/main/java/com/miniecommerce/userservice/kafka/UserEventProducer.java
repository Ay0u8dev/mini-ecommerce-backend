package com.miniecommerce.userservice.kafka;

import com.miniecommerce.userservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUserEvent(UserEvent event) {
        log.info("Publishing user event: {} for user ID: {}", event.getEventType(), event.getUserId());

        CompletableFuture<SendResult<String, UserEvent>> future =
                kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ User event published successfully: {} - Partition: {}, Offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to publish user event: {}", ex.getMessage());
            }
        });
    }
}