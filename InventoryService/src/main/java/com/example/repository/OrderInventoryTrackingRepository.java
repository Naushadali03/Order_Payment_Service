package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.OrderInventoryTracking;

import java.util.Optional;

@Repository
public interface OrderInventoryTrackingRepository extends JpaRepository<OrderInventoryTracking, Long> {
    
    Optional<OrderInventoryTracking> findByOrderId(String orderId);
    
    boolean existsByOrderId(String orderId);
}
