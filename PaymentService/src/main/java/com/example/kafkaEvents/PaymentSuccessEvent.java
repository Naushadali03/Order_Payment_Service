package com.example.kafkaEvents;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private String orderId;
    private String paymentId;
    private String customerId;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime timestamp;

    public PaymentSuccessEvent(String orderId, String paymentId, String customerId, 
                              BigDecimal amount, String transactionId) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
    }
}
