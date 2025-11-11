package com.miniecommerce.orderservice.kafka;

import com.miniecommerce.orderservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderEvent(OrderEvent event) {
        log.info("Publishing order event: {} for order ID: {}", event.getEventType(), event.getOrderId());

        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Order event published successfully: {} - Partition: {}, Offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to publish order event: {}", ex.getMessage());
            }
        });
    }
}