package com.example.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.constant.PaymentStatus;
import com.example.entity.Payment;
import com.example.kafkaEvents.OrderCreatedEvent;
import com.example.repository.PaymentRepository;
import com.example.service.PaymentSerivice;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentSerivice{

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository){
        this.paymentRepository=paymentRepository;
    }
    
    @Override
    @Transactional
    public void processPayment(OrderCreatedEvent orderCreatedEvent){
        log.info("Processing payment for orderId: {}",orderCreatedEvent.getOrderId());
        Payment payment = new Payment();
        payment.setPaymentId(generatedPaymentId());
        payment.setOrderId(orderCreatedEvent.getOrderId());
        payment.setCustomerId(orderCreatedEvent.getCustomerId());
        payment.setAmount(orderCreatedEvent.getTotalAmount());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setPaymentMethod("CREDIT_CARD");
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment Record Created with ID: {}",savedPayment.getPaymentId());
    }

    private String generatedPaymentId(){
        return "PAY-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }
}
