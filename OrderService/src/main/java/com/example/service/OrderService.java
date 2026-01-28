package com.example.service;

import com.example.dto.OrderRequest;
import com.example.dto.OrderResponse;

public interface OrderService {
    public OrderResponse createOrder(OrderRequest orderRequest);
    public OrderResponse getOrder(String orderId);
}
