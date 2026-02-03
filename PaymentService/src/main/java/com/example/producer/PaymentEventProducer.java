package com.example.producer;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.kafkaEvents.PaymentFailedEvent;
import com.example.kafkaEvents.PaymentSuccessEvent;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentEventProducer {
    
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Value("${kafka.topic.payment-events}")
    private String paymentEvents;

    public PaymentEventProducer(KafkaTemplate kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }

    public void sendPaymentSuccessEvents(PaymentSuccessEvent events){
        log.info("Publishing Payment Success Event for orderId: {}",events.getOrderId());
        CompletableFuture<SendResult<String,Object>> future = kafkaTemplate.send(paymentEvents,events.getOrderId(),events);

        future.whenComplete((result,ex)->{
                if(ex==null){
                    log.info("Sent Successfully paymentSuccessEvent for orderId: {} to topic: {}",events.getOrderId(),paymentEvents);
                }
                else{
                    log.info("Failed to sent paymentSuccessEvent for orderId: {}", events.getOrderId(),ex);
                }
        }) ;
    }

    public void sendPaymentFailedEvents(PaymentFailedEvent event){
        log.info("Publishing payment failed event for orderId: {}",event.getOrderId());
        CompletableFuture<SendResult<String,Object>> future = kafkaTemplate.send(paymentEvents,event.getOrderId(),event);
        future.whenComplete((result,ex)->{
            if(ex==null){
                log.info("Sent successfully payment failed event for orderId: {} to topic: {}", event.getOrderId(),paymentEvents);
            }
            else{
                log.info("Failed to send payment failed event for orderId: {}", event.getOrderId(),ex);
            }
        });
    }
}