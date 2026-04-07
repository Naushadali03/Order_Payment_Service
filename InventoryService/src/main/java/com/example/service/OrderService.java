package com.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.dto.OrderDetails;
import com.example.dto.OrderResponse;

import java.math.BigDecimal;

@Service
@Slf4j
public class OrderService {

    private final RestTemplate restTemplate;
    private static final String ORDER_SERVICE_URL = "http://localhost:8081/api/orders";

    public OrderService() {
        this.restTemplate = new RestTemplate();
    }

    public OrderDetails getOrderDetails(String orderId) {
        try {
            log.info("Fetching order details from Order Service for orderId: {}", orderId);
            
            String url = ORDER_SERVICE_URL + "/" + orderId;
            OrderResponse response = restTemplate.getForObject(url, OrderResponse.class);
            
            if (response == null) {
                throw new RuntimeException("Order not found: " + orderId);
            }

            return new OrderDetails(
                response.getOrderId(),
                response.getProductId(),
                response.getQuantity()
            );
            
        } catch (Exception e) {
            log.error("Error fetching order details for orderId: {}", orderId, e);
            throw new RuntimeException("Failed to fetch order details", e);
        }
    }

}
