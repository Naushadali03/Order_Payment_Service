package com.example.kafkaEvents;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private String orderId;
    private String paymentId;
    private String customerId;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime timestamp;

    public PaymentFailedEvent(String orderId, String paymentId, String customerId, 
                             BigDecimal amount, String reason) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.amount = amount;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
}
