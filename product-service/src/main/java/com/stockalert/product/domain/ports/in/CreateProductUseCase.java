package com.stockalert.product.domain.ports.in;

import com.stockalert.product.domain.model.Product;

/**
 * Input port (Use Case) for creating a product.
 * This interface defines the contract for creating products in the domain.
 */
public interface CreateProductUseCase {
    
    /**
     * Creates a new product in the system
     * 
     * @param product the product to create
     * @return the created product with generated ID
     * @throws IllegalArgumentException if the product is invalid
     */
    Product createProduct(Product product);
}
