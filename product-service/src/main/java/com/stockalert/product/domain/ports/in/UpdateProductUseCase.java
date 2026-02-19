package com.stockalert.product.domain.ports.in;

import com.stockalert.product.domain.model.Product;

import java.util.Optional;

/**
 * Input port (Use Case) for updating a product.
 * This interface defines the contract for updating products in the domain.
 */
public interface UpdateProductUseCase {
    
    /**
     * Updates an existing product
     * 
     * @param id the ID of the product to update
     * @param product the product with updated data
     * @return the updated product if found
     * @throws IllegalArgumentException if the product is invalid
     */
    Optional<Product> updateProduct(Long id, Product product);
}
