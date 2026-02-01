package com.example.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.kafkaEvents.OrderCreatedEvent;
import com.example.service.PaymentSerivice;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderEventConsumer {
    private final PaymentSerivice paymentSerivice;

    public OrderEventConsumer(PaymentSerivice paymentSerivice){
        this.paymentSerivice=paymentSerivice;
    }

    @KafkaListener(topics = "${kafka.topic.order-events}",
                    groupId = "${spring.kafka.consumer.group-id}",
                    containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(OrderCreatedEvent event){
        log.info("========================================");
        log.info("Recieved orderCreatedEvent from kafka");
        log.info("OrderId: {}", event.getOrderId());
        log.info("CustomerId: {}", event.getCustomerId());
        log.info("ProductId: {}", event.getProductId());
        log.info("Quantity: {}", event.getQuantity());
        log.info("Amount: {}", event.getTotalAmount());
        log.info("========================================");

        try{
            paymentSerivice.processPayment(event);
        }
        catch(Exception e){
            log.error("Error processing payment for orderId: {}", event.getOrderId(), e);
        }
    }

}
