package com.example.kafkaProducer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.kafkaEvent.InventoryUpdatedEvent;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate;
    
    @Value("${kafka.topic.inventory-events}")
    private String inventoryEventsTopic;

    public InventoryEventProducer(KafkaTemplate<String, InventoryUpdatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryUpdatedEvent(InventoryUpdatedEvent event) {
        log.info("Publishing InventoryUpdatedEvent for orderId: {}, productId: {}", 
            event.getOrderId(), event.getProductId());
        
        CompletableFuture<SendResult<String, InventoryUpdatedEvent>> future = 
            kafkaTemplate.send(inventoryEventsTopic, event.getOrderId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent InventoryUpdatedEvent for orderId: {} to topic: {}", 
                    event.getOrderId(), inventoryEventsTopic);
                log.info("Stock updated: {} -> {}", event.getOldStock(), event.getNewStock());
            } else {
                log.error("Failed to send InventoryUpdatedEvent for orderId: {}", 
                    event.getOrderId(), ex);
            }
        });
    }
}