package com.example.service.serviceImpl;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.protocol.types.Field.Str;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.constants.OrderStatus;
import com.example.dto.OrderRequest;
import com.example.dto.OrderResponse;
import com.example.entity.Order;
import com.example.kafkaEvent.OrderCreatedEvent;
import com.example.orderProducer.OrderEventProducer;
import com.example.repository.OrderRepository;
import com.example.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Transactional
    @Override
    public OrderResponse createOrder(OrderRequest request){
        log.info("Creating Order for customer: {}",request.getCustomerId());

        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(OrderStatus.PENDING);

        Order saveOrder = orderRepository.save(order);
        log.info("Order saved to Database with Id", saveOrder.getOrderId());
        
        cacheOrder(saveOrder);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
            saveOrder.getOrderId(),
            saveOrder.getCustomerId(),
            saveOrder.getProductId(),
            saveOrder.getQuantity(),
            saveOrder.getTotalAmount()
        );

        orderEventProducer.sendOrderCreatedEvent(orderCreatedEvent);
    
        return mapToResponse(saveOrder, "Order Create Sucessfully");
    }

    private void cacheOrder(Order order){
        String orderKey = "Order: "+order.getOrderId();
        redisTemplate.opsForValue().set(orderKey, order,1,TimeUnit.HOURS);
        log.info("Order cached in Redis with Key: {}",orderKey);
    }

    private OrderResponse mapToResponse(Order order,String message){
        return new OrderResponse(order.getOrderId(),
            order.getCustomerId(),
            order.getProductId(),
            order.getQuantity(),
            order.getTotalAmount(),
            order.getStatus().toString(),
            order.getCreatedAt().toString(),
            message);
    }

    public String generateOrderId(){
        return "ORD-"+UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
