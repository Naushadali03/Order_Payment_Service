package com.example.service;

import com.example.kafkaEvents.OrderCreatedEvent;

public interface PaymentSerivice {

    public void processPayment(OrderCreatedEvent orderEvent);
}
