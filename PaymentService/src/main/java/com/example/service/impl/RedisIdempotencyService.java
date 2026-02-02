package com.example.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisIdempotencyService {
    
    private final RedisTemplate<String,Object> redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "payment:processed:";
    private static final Long TTL_HOURS = 24L;

    public RedisIdempotencyService(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public boolean isAlreadyProcessed(String orderId){
        String key = IDEMPOTENCY_PREFIX + orderId;
        Boolean exists = redisTemplate.hasKey(key);
        if(Boolean.TRUE.equals(exists)){
            log.warn("Payment alrady processed for orderId: {}",orderId);
            return true;
        }
        return false;
    }

    public void markAsProcessed(String orderId, String paymentId){
        String key = IDEMPOTENCY_PREFIX + orderId;
        redisTemplate.opsForValue().set(key, paymentId,TTL_HOURS,TimeUnit.HOURS);
        log.info("Marked orderId: {} as processed with paymentId: {}",orderId,paymentId);
    }

    public String getProcessedPaymentId(String orderId){
        String key = IDEMPOTENCY_PREFIX+orderId;
        Object paymentId = redisTemplate.opsForValue().get(key);
        return paymentId!=null ? paymentId.toString() : null;
    }

}
