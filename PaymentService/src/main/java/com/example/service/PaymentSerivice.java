package com.example.service;

import java.util.List;

import com.example.entity.Payment;
import com.example.kafkaEvents.OrderCreatedEvent;

public interface PaymentSerivice {

    public void processPayment(OrderCreatedEvent orderEvent);

    public Payment getPaymentByOrderId(String orderId);

    public Payment getPaymentById(String paymentId);

    public List<Payment> getAllPayment();
}
