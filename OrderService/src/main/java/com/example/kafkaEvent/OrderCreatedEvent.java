package com.example.kafkaEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {   
    private String orderId;
    private String customerId;
    public OrderCreatedEvent(String orderId, String customerId, String productId, Integer quantity,
            BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.timestamp = LocalDateTime.now();
    }
    private String productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}
