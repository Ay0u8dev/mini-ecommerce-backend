package com.miniecommerce.productservice.kafka;

import com.miniecommerce.productservice.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private static final String TOPIC = "product-events";

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public void sendProductEvent(ProductEvent event) {
        log.info("Publishing product event: {} for product ID: {}", event.getEventType(), event.getProductId());

        CompletableFuture<SendResult<String, ProductEvent>> future =
                kafkaTemplate.send(TOPIC, event.getProductId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Product event published successfully: {} - Partition: {}, Offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to publish product event: {}", ex.getMessage());
            }
        });
    }
}