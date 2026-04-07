package com.example.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.kafkaEvent.PaymentSuccessEvent;
import com.example.service.InventoryService;

@Component
@Slf4j
public class PaymentEventConsumer {

    private final InventoryService inventoryService;

    public PaymentEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
        topics = "${kafka.topic.payment-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(Object event) {
        log.info("========================================");
        log.info("Received event from payment-events topic");
        
        
        if (event instanceof PaymentSuccessEvent) {
            PaymentSuccessEvent successEvent = (PaymentSuccessEvent) event;
            
            log.info("Event Type: PaymentSuccessEvent");
            log.info("OrderId: {}", successEvent.getOrderId());
            log.info("PaymentId: {}", successEvent.getPaymentId());
            log.info("CustomerId: {}", successEvent.getCustomerId());
            log.info("Amount: {}", successEvent.getAmount());
            log.info("TransactionId: {}", successEvent.getTransactionId());
            log.info("========================================");

            try {
               
                inventoryService.processPaymentSuccess(successEvent);
                
            } catch (Exception e) {
                log.error("Error processing inventory for orderId: {}", 
                    successEvent.getOrderId(), e);
                
            }
        } else {
            
            log.info("Ignoring non-success payment event");
            log.info("========================================");
        }
    }
}
