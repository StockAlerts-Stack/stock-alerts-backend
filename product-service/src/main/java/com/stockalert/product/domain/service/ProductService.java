package com.stockalert.product.domain.service;

import com.stockalert.product.domain.model.Product;
import com.stockalert.product.domain.model.ProductStockChangedEvent;
import com.stockalert.product.domain.ports.in.CreateProductUseCase;
import com.stockalert.product.domain.ports.in.GetProductUseCase;
import com.stockalert.product.domain.ports.in.UpdateProductUseCase;
import com.stockalert.product.domain.ports.out.ProductEventPublisherPort;
import com.stockalert.product.domain.ports.out.ProductRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Domain service implementing all product use cases.
 * This class contains the core business logic and has no Spring annotations.
 * It will be configured as a bean in the infrastructure layer.
 */
public class ProductService implements CreateProductUseCase, UpdateProductUseCase, GetProductUseCase {
    
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductEventPublisherPort eventPublisherPort;
    
    /**
     * Constructor for dependency injection
     * 
     * @param productRepositoryPort the repository port implementation
     * @param eventPublisherPort the event publisher port implementation
     */
    public ProductService(ProductRepositoryPort productRepositoryPort, 
                         ProductEventPublisherPort eventPublisherPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }
    
    @Override
    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        if (!product.isValid()) {
            throw new IllegalArgumentException("Product data is invalid");
        }
        
        // Check if SKU already exists
        if (product.getSku() != null && productRepositoryPort.findBySku(product.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
        }
        
        Product savedProduct = productRepositoryPort.save(product);
        
        // Publish product created event
        publishProductCreatedEvent(savedProduct);
        
        return savedProduct;
    }
    
    @Override
    public Optional<Product> updateProduct(Long id, Product product) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        if (!product.isValid()) {
            throw new IllegalArgumentException("Product data is invalid");
        }
        
        return productRepositoryPort.findById(id)
                .map(existingProduct -> {
                    // Check if SKU is being changed and if new SKU already exists
                    if (!existingProduct.getSku().equals(product.getSku())) {
                        productRepositoryPort.findBySku(product.getSku())
                                .ifPresent(p -> {
                                    throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
                                });
                    }
                    
                    // Capture old stock before update
                    Integer oldStock = existingProduct.getStock();
                    
                    // Update fields
                    existingProduct.setSku(product.getSku());
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setStock(product.getStock());
                    
                    Product updatedProduct = productRepositoryPort.save(existingProduct);
                    
                    // Publish stock change event if stock changed
                    if (!oldStock.equals(updatedProduct.getStock())) {
                        publishStockChangedEvent(updatedProduct, oldStock);
                    }
                    
                    return updatedProduct;
                });
    }
    
    @Override
    public Optional<Product> getProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productRepositoryPort.findById(id);
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }
    
    @Override
    public Optional<Product> getProductBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        return productRepositoryPort.findBySku(sku);
    }
    
    /**
     * Publishes an event when a product is created
     */
    private void publishProductCreatedEvent(Product product) {
        ProductStockChangedEvent event = ProductStockChangedEvent.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .oldStock(0)
                .newStock(product.getStock())
                .timestamp(LocalDateTime.now())
                .eventType(ProductStockChangedEvent.EventType.PRODUCT_CREATED)
                .build();
        
        eventPublisherPort.publishStockChangeEvent(event);
    }
    
    /**
     * Publishes an event when product stock changes
     */
    private void publishStockChangedEvent(Product product, Integer oldStock) {
        ProductStockChangedEvent.EventType eventType;
        
        if (product.getStock() > oldStock) {
            eventType = ProductStockChangedEvent.EventType.STOCK_INCREASED;
        } else if (product.getStock() < oldStock) {
            eventType = ProductStockChangedEvent.EventType.STOCK_DECREASED;
        } else {
            eventType = ProductStockChangedEvent.EventType.STOCK_UPDATED;
        }
        
        ProductStockChangedEvent event = ProductStockChangedEvent.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .oldStock(oldStock)
                .newStock(product.getStock())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .build();
        
        eventPublisherPort.publishStockChangeEvent(event);
    }
}
