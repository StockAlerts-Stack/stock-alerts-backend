package com.stockalert.product.infrastructure.adapters.out.persistence.adapter;

import com.stockalert.product.domain.model.Product;
import com.stockalert.product.domain.ports.out.ProductRepositoryPort;
import com.stockalert.product.infrastructure.adapters.out.persistence.entity.ProductEntity;
import com.stockalert.product.infrastructure.adapters.out.persistence.mapper.ProductMapper;
import com.stockalert.product.infrastructure.adapters.out.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the ProductRepositoryPort.
 * This class bridges the domain layer with the persistence infrastructure.
 */
@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    
    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;
    
    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository, ProductMapper productMapper) {
        this.productJpaRepository = productJpaRepository;
        this.productMapper = productMapper;
    }
    
    @Override
    public Product save(Product product) {
        ProductEntity entity = productMapper.toEntity(product);
        ProductEntity savedEntity = productJpaRepository.save(entity);
        return productMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(productMapper::toDomain);
    }
    
    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll()
                .stream()
                .map(productMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Product> findBySku(String sku) {
        return productJpaRepository.findBySku(sku)
                .map(productMapper::toDomain);
    }
    
    @Override
    public void deleteById(Long id) {
        productJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return productJpaRepository.existsById(id);
    }
}
