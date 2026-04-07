package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.entity.OrderInventoryTracking;
import com.example.entity.Product;
import com.example.kafkaEvent.InventoryUpdatedEvent;
import com.example.kafkaEvent.PaymentSuccessEvent;
import com.example.kafkaProducer.InventoryEventProducer;
import com.example.repository.OrderInventoryTrackingRepository;
import com.example.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class InventoryService {
    private final ProductRepository productRepository;
    private final OrderInventoryTrackingRepository trackingRepository;
    private final InventoryEventProducer inventoryEventProducer;
    private final OrderService orderService;

    public InventoryService(ProductRepository productRepository,
                           OrderInventoryTrackingRepository trackingRepository,
                           InventoryEventProducer inventoryEventProducer,
                           OrderService orderService) {
        this.productRepository = productRepository;
        this.trackingRepository = trackingRepository;
        this.inventoryEventProducer = inventoryEventProducer;
        this.orderService = orderService;
    }

    @Transactional
    public void processPaymentSuccess(PaymentSuccessEvent paymentEvent) {
        String orderId = paymentEvent.getOrderId();
        log.info("Processing inventory update for orderId: {}", orderId);

        
        if (trackingRepository.existsByOrderId(orderId)) {
            log.warn("Inventory already updated for orderId: {}. Skipping.", orderId);
            return;
        }

        var orderDetails = orderService.getOrderDetails(orderId);
        
        String productId = orderDetails.getProductId();
        Integer quantity = orderDetails.getQuantity();

       
        Product product = getProductByIdInternal(productId);

        
        if (!product.hasStock(quantity)) {
            log.error("Insufficient stock for productId: {}. Available: {}, Required: {}", 
                productId, product.getAvailableQuantity(), quantity);
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }

        
        Integer oldStock = product.getStockQuantity();
        product.reduceStock(quantity);
        productRepository.save(product);
        
        log.info("Stock updated for productId: {}. Old: {}, New: {}", 
            productId, oldStock, product.getStockQuantity());

        
        evictProductCache(productId);

       
        OrderInventoryTracking tracking = new OrderInventoryTracking();
        tracking.setOrderId(orderId);
        tracking.setProductId(productId);
        tracking.setQuantityProcessed(quantity);
        tracking.setInventoryUpdated(true);
        trackingRepository.save(tracking);

        
        InventoryUpdatedEvent event = new InventoryUpdatedEvent(
            orderId,
            productId,
            quantity,
            oldStock,
            product.getStockQuantity()
        );
        inventoryEventProducer.sendInventoryUpdatedEvent(event);

        log.info("Inventory update completed for orderId: {}", orderId);
    }

   

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getProductId());

        if (productRepository.existsByProductId(request.getProductId())) {
            throw new RuntimeException("Product already exists: " + request.getProductId());
        }

        Product product = new Product();
        product.setProductId(request.getProductId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}", savedProduct.getProductId());

        return mapToResponse(savedProduct);
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProductById(String productId) {
        log.info("Fetching product from database: {}", productId);
        
        Product product = productRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        return mapToResponse(product);
    }

   
    private Product getProductByIdInternal(String productId) {
        return productRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        
        return productRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse updateStock(String productId, Integer quantity, String operation) {
        log.info("Updating stock for productId: {}, operation: {}, quantity: {}", 
            productId, operation, quantity);

        Product product = getProductByIdInternal(productId);

        if ("ADD".equalsIgnoreCase(operation)) {
            product.setStockQuantity(product.getStockQuantity() + quantity);
        } else if ("REDUCE".equalsIgnoreCase(operation)) {
            if (!product.hasStock(quantity)) {
                throw new RuntimeException("Insufficient stock");
            }
            product.reduceStock(quantity);
        } else {
            throw new RuntimeException("Invalid operation: " + operation);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated for productId: {}, new stock: {}", 
            productId, updatedProduct.getStockQuantity());

        return mapToResponse(updatedProduct);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void evictProductCache(String productId) {
        log.info("Evicting cache for productId: {}", productId);
    }

   

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getAvailableQuantity(),
            product.getCreatedAt().toString(),
            product.getUpdatedAt().toString()
        );
    }
}
