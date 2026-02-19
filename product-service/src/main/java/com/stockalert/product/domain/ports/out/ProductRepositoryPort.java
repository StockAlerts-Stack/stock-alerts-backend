package com.stockalert.product.domain.ports.out;

import com.stockalert.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Product persistence.
 * This interface defines the contract that the persistence adapter must implement.
 * No infrastructure dependencies should be present here.
 */
public interface ProductRepositoryPort {
    
    /**
     * Saves a product (create or update)
     * 
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);
    
    /**
     * Finds a product by its ID
     * 
     * @param id the product ID
     * @return the product if found
     */
    Optional<Product> findById(Long id);
    
    /**
     * Finds all products
     * 
     * @return list of all products
     */
    List<Product> findAll();
    
    /**
     * Finds a product by its SKU
     * 
     * @param sku the product SKU
     * @return the product if found
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Deletes a product by its ID
     * 
     * @param id the product ID
     */
    void deleteById(Long id);
    
    /**
     * Checks if a product exists by its ID
     * 
     * @param id the product ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
}
