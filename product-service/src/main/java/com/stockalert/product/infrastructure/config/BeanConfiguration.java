package com.stockalert.product.infrastructure.config;

import com.stockalert.product.domain.ports.in.CreateProductUseCase;
import com.stockalert.product.domain.ports.in.GetProductUseCase;
import com.stockalert.product.domain.ports.in.UpdateProductUseCase;
import com.stockalert.product.domain.ports.out.ProductEventPublisherPort;
import com.stockalert.product.domain.ports.out.ProductRepositoryPort;
import com.stockalert.product.domain.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for domain beans.
 * This class manually defines beans for domain services to avoid using
 * Spring annotations in the domain layer, maintaining clean architecture.
 */
@Configuration
public class BeanConfiguration {
    
    /**
     * Creates the ProductService bean which implements all product use cases.
     * 
     * @param productRepositoryPort the repository port implementation (provided by adapter)
     * @param eventPublisherPort the event publisher port implementation (provided by adapter)
     * @return the ProductService instance
     */
    @Bean
    public ProductService productService(ProductRepositoryPort productRepositoryPort,
                                        ProductEventPublisherPort eventPublisherPort) {
        return new ProductService(productRepositoryPort, eventPublisherPort);
    }
    
    /**
     * Provides the CreateProductUseCase bean.
     * This allows dependency injection of the specific use case interface.
     * 
     * @param productService the product service implementation
     * @return the CreateProductUseCase implementation
     */
    @Bean
    public CreateProductUseCase createProductUseCase(ProductService productService) {
        return productService;
    }
    
    /**
     * Provides the UpdateProductUseCase bean.
     * This allows dependency injection of the specific use case interface.
     * 
     * @param productService the product service implementation
     * @return the UpdateProductUseCase implementation
     */
    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductService productService) {
        return productService;
    }
    
    /**
     * Provides the GetProductUseCase bean.
     * This allows dependency injection of the specific use case interface.
     * 
     * @param productService the product service implementation
     * @return the GetProductUseCase implementation
     */
    @Bean
    public GetProductUseCase getProductUseCase(ProductService productService) {
        return productService;
    }
}
