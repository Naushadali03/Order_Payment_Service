package com.example.kafkaEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
