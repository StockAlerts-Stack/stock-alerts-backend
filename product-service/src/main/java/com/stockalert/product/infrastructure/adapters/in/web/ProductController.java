package com.stockalert.product.infrastructure.adapters.in.web;

import com.stockalert.product.domain.model.Product;
import com.stockalert.product.domain.ports.in.CreateProductUseCase;
import com.stockalert.product.domain.ports.in.GetProductUseCase;
import com.stockalert.product.domain.ports.in.UpdateProductUseCase;
import com.stockalert.product.infrastructure.adapters.in.web.dto.ProductRequest;
import com.stockalert.product.infrastructure.adapters.in.web.dto.ProductResponse;
import com.stockalert.product.infrastructure.adapters.in.web.mapper.ProductDtoMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.stockalert.product.infrastructure.config.CacheConfig.PRODUCT_CACHE;
import static com.stockalert.product.infrastructure.config.CacheConfig.PRODUCTS_CACHE;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ProductDtoMapper productDtoMapper;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            GetProductUseCase getProductUseCase,
            ProductDtoMapper productDtoMapper) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.productDtoMapper = productDtoMapper;
    }

    @PostMapping
    @Caching(evict = {
            @CacheEvict(value = PRODUCTS_CACHE, allEntries = true)
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("POST /api/v1/products - sku={}", request.getSku());
        Product product = productDtoMapper.toDomain(request);
        Product createdProduct = createProductUseCase.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDtoMapper.toResponse(createdProduct));
    }

    @PutMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(value = PRODUCT_CACHE, key = "#id"),
            @CacheEvict(value = PRODUCTS_CACHE, allEntries = true)
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("PUT /api/v1/products/{}", id);
        Product product = productDtoMapper.toDomain(request);
        return updateProductUseCase.updateProduct(id, product)
                .map(updated -> ResponseEntity.ok(productDtoMapper.toResponse(updated)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Cacheable(value = PRODUCT_CACHE, key = "#id")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.debug("GET /api/v1/products/{}", id);
        return getProductUseCase.getProductById(id)
                .map(product -> ResponseEntity.ok(productDtoMapper.toResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Cacheable(value = PRODUCTS_CACHE, key = "'all'")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.debug("GET /api/v1/products");
        List<ProductResponse> response = getProductUseCase.getAllProducts().stream()
                .map(productDtoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.debug("GET /api/v1/products/sku/{}", sku);
        return getProductUseCase.getProductBySku(sku)
                .map(product -> ResponseEntity.ok(productDtoMapper.toResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }
}
