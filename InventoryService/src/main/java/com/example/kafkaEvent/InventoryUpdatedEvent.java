package com.example.kafkaEvent;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    private String orderId;
    private String productId;
    private Integer quantityDeducted;
    private Integer oldStock;
    private Integer newStock;
    private LocalDateTime timestamp;

    public InventoryUpdatedEvent(String orderId, String productId, 
                                Integer quantityDeducted, Integer oldStock, Integer newStock) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantityDeducted = quantityDeducted;
        this.oldStock = oldStock;
        this.newStock = newStock;
        this.timestamp = LocalDateTime.now();
    }
}
