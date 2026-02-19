package com.stockalert.product.infrastructure.adapters.in.web.mapper;

import com.stockalert.product.domain.model.Product;
import com.stockalert.product.infrastructure.adapters.in.web.dto.ProductRequest;
import com.stockalert.product.infrastructure.adapters.in.web.dto.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between DTOs and Domain Models.
 */
@Component
public class ProductDtoMapper {
    
    /**
     * Converts a ProductRequest to a domain Product
     * 
     * @param request the request DTO
     * @return the domain product
     */
    public Product toDomain(ProductRequest request) {
        if (request == null) {
            return null;
        }
        
        return Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
    }
    
    /**
     * Converts a domain Product to a ProductResponse
     * 
     * @param product the domain product
     * @return the response DTO
     */
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}
