package com.example.orderProducer;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.kafkaEvent.OrderCreatedEvent;


import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String,OrderCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event){
        log.info("Publishing OrderCreatedEvent: {}",event);
        CompletableFuture<SendResult<String,OrderCreatedEvent>> future = kafkaTemplate.send(orderEventsTopic,event.getOrderId(),event);
        future.whenComplete((result,ex)->{
            if(ex==null){
                 log.info("Successfully sent OrderCreatedEvent for orderId: {} to topic: {}", 
                    event.getOrderId(), orderEventsTopic);
            }
            else{
                log.error("Failed to send OrderCreatedEvent for orderId: {}", 
                    event.getOrderId(), ex);
            }
        });
    }


}
