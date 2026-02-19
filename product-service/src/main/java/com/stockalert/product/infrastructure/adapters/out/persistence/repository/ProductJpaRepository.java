package com.stockalert.product.infrastructure.adapters.out.persistence.repository;

import com.stockalert.product.infrastructure.adapters.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for ProductEntity.
 * This interface provides CRUD operations and custom queries.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    
    /**
     * Finds a product by its SKU
     * 
     * @param sku the product SKU
     * @return the product entity if found
     */
    Optional<ProductEntity> findBySku(String sku);
    
    /**
     * Checks if a product exists by SKU
     * 
     * @param sku the product SKU
     * @return true if exists, false otherwise
     */
    boolean existsBySku(String sku);
}
