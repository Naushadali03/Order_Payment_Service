package com.example.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.dto.StockUpdateRequest;
import com.example.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("Received request to create product: {}", request.getProductId());
        
        ProductResponse response = inventoryService.createProduct(request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
        log.info("Received request to fetch product: {}", productId);
        
        ProductResponse response = inventoryService.getProductById(productId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Received request to fetch all products");
        
        List<ProductResponse> products = inventoryService.getAllProducts();
        
        return ResponseEntity.ok(products);
    }

    @PutMapping("/products/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody StockUpdateRequest request) {
        
        log.info("Received request to update stock for productId: {}, operation: {}, quantity: {}", 
            productId, request.getOperation(), request.getQuantity());
        
        ProductResponse response = inventoryService.updateStock(
            productId, 
            request.getQuantity(), 
            request.getOperation()
        );
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{productId}/cache")
    public ResponseEntity<String> evictCache(@PathVariable String productId) {
        log.info("Received request to evict cache for productId: {}", productId);
        
        inventoryService.evictProductCache(productId);
        
        return ResponseEntity.ok("Cache evicted for product: " + productId);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Inventory Service is running!");
    }
}