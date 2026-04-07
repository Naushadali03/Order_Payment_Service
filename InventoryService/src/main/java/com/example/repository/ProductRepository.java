package com.example.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    
    Optional<Product> findByProductId(String productId);
    
    boolean existsByProductId(String productId);
}
