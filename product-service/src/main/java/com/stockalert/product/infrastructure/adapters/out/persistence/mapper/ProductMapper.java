package com.stockalert.product.infrastructure.adapters.out.persistence.mapper;

import com.stockalert.product.domain.model.Product;
import com.stockalert.product.infrastructure.adapters.out.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between Domain Model and JPA Entity.
 * This ensures separation between domain and infrastructure layers.
 */
@Component
public class ProductMapper {
    
    /**
     * Converts a domain Product to a ProductEntity
     * 
     * @param product the domain product
     * @return the JPA entity
     */
    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductEntity.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
    
    /**
     * Converts a ProductEntity to a domain Product
     * 
     * @param entity the JPA entity
     * @return the domain product
     */
    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Product.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .build();
    }
}
