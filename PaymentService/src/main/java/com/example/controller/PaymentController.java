package com.example.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Payment;
import com.example.service.PaymentSerivice;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final PaymentSerivice paymentSerivice;

    public PaymentController(PaymentSerivice paymentSerivice){
        this.paymentSerivice=paymentSerivice;
    }


    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId){
        log.info("Request received to fetch payment for orderId: {}",orderId);
        Payment payment = paymentSerivice.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String paymentId){
        log.info("Request received to fetch payment for paymentId: {}",paymentId);
        Payment payment = paymentSerivice.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayment(){
        log.info("Request received to fetch all payment");
        List<Payment> payments = paymentSerivice.getAllPayment();
        return ResponseEntity.ok(payments);
    }
    
}
