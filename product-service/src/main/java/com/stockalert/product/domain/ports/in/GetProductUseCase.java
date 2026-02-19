package com.stockalert.product.domain.ports.in;

import com.stockalert.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Input port (Use Case) for retrieving products.
 * This interface defines the contract for querying products in the domain.
 */
public interface GetProductUseCase {
    
    /**
     * Retrieves a product by its ID
     * 
     * @param id the product ID
     * @return the product if found
     */
    Optional<Product> getProductById(Long id);
    
    /**
     * Retrieves all products
     * 
     * @return list of all products
     */
    List<Product> getAllProducts();
    
    /**
     * Retrieves a product by its SKU
     * 
     * @param sku the product SKU
     * @return the product if found
     */
    Optional<Product> getProductBySku(String sku);
}
