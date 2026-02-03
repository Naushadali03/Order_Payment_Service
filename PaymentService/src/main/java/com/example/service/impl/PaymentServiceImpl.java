package com.example.service.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.constant.PaymentStatus;
import com.example.entity.Payment;
import com.example.kafkaEvents.OrderCreatedEvent;
import com.example.kafkaEvents.PaymentFailedEvent;
import com.example.kafkaEvents.PaymentSuccessEvent;
import com.example.producer.PaymentEventProducer;
import com.example.repository.PaymentRepository;
import com.example.service.PaymentSerivice;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentSerivice{

    private final PaymentRepository paymentRepository;
    private final RedisIdempotencyService idempotencyService;
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    @Value("${payment.success-rate: 80}")
    private int successRate;

    @Value("${payment.processing-delay: 2000}")
    private Long processingDelay;

    public PaymentServiceImpl(PaymentRepository paymentRepository, RedisIdempotencyService idempotencyService,PaymentEventProducer paymentEventProducer){
        this.paymentRepository=paymentRepository;
        this.idempotencyService=idempotencyService;
        this.paymentEventProducer=paymentEventProducer;
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

        try{
            Thread.sleep(processingDelay);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            log.info("Payment Processing Interupted", e);
        }

        boolean paymentProcess = simulatePaymentProcessing();

        if(paymentProcess){
            String transactionId = generateTransactionId();
            savedPayment.setStatus(PaymentStatus.COMPLETED);
            savedPayment.setTransactionId(transactionId);
            paymentRepository.save(savedPayment);
            log.info("PAYMENT SUCCESSFULL for order Id: {}, paymentId: {}",savedPayment.getOrderId(),savedPayment.getPaymentId());

            idempotencyService.markAsProcessed(savedPayment.getOrderId(), savedPayment.getPaymentId());

             PaymentSuccessEvent successEvent = new PaymentSuccessEvent(
                savedPayment.getOrderId(),
                savedPayment.getPaymentId(),
                orderCreatedEvent.getCustomerId(),
                orderCreatedEvent.getTotalAmount(),
                transactionId
            );
            paymentEventProducer.sendPaymentSuccessEvents(successEvent);  
        } else{
            String failureReason = "Insufficient funds / Card declined";
            savedPayment.setFailureReason(failureReason);
            savedPayment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(savedPayment);
            log.error("Payment FAILED for orderId: {}, reason: {}", orderCreatedEvent.getOrderId(), failureReason);

            idempotencyService.markAsProcessed(orderCreatedEvent.getOrderId(), savedPayment.getPaymentId());

            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                orderCreatedEvent.getOrderId(),
                savedPayment.getPaymentId(),
                orderCreatedEvent.getCustomerId(),
                orderCreatedEvent.getTotalAmount(),
                failureReason
            );

            paymentEventProducer.sendPaymentFailedEvents(failedEvent);

        }

    }

    @Override
    public Payment getPaymentByOrderId(String orderId){
       Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));

            return payment;
    }

    public Payment getPaymentById(String paymentId){
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found for paymentId: " + paymentId));

            return payment;
    }

    public List<Payment> getAllPayment(){
        return paymentRepository.findAll();
    }

    private boolean simulatePaymentProcessing(){
        int randomValue = random.nextInt(100);
       return randomValue < successRate;
    }

    private String generatedPaymentId(){
        return "PAY-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }

    private String generateTransactionId(){
        return "TXN-"+UUID.randomUUID().toString().substring(0,12).toUpperCase();
    }
}
