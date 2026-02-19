package com.stockalert.inventory.infrastructure.adapters.web;

import com.stockalert.inventory.domain.model.Inventory;
import com.stockalert.inventory.domain.ports.in.CreateInventoryUseCase;
import com.stockalert.inventory.domain.ports.in.DecreaseStockUseCase;
import com.stockalert.inventory.domain.ports.in.GetInventoryUseCase;
import com.stockalert.inventory.domain.ports.in.IncreaseStockUseCase;
import com.stockalert.inventory.infrastructure.adapters.web.dto.InventoryRequest;
import com.stockalert.inventory.infrastructure.adapters.web.dto.InventoryResponse;
import com.stockalert.inventory.infrastructure.adapters.web.dto.StockUpdateRequest;
import com.stockalert.inventory.infrastructure.adapters.web.mapper.InventoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.stockalert.inventory.infrastructure.config.CacheConfig.INVENTORY_CACHE;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final GetInventoryUseCase getInventoryUseCase;
    private final CreateInventoryUseCase createInventoryUseCase;
    private final DecreaseStockUseCase decreaseStockUseCase;
    private final IncreaseStockUseCase increaseStockUseCase;
    private final InventoryMapper inventoryMapper;

    @GetMapping("/{productId}")
    @Cacheable(value = INVENTORY_CACHE, key = "#productId")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable Long productId) {
        log.debug("GET /api/v1/inventory/{}", productId);
        Inventory inventory = getInventoryUseCase.getByProductId(productId);
        return ResponseEntity.ok(inventoryMapper.toResponse(inventory));
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryRequest request) {
        log.info("POST /api/v1/inventory - productId={}", request.getProductId());
        Inventory inventory = createInventoryUseCase.create(
                request.getProductId(),
                request.getQuantity(),
                request.getThreshold()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMapper.toResponse(inventory));
    }

    @PutMapping("/{productId}/decrease")
    @CacheEvict(value = INVENTORY_CACHE, key = "#productId")
    public ResponseEntity<InventoryResponse> decrease(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("PUT /api/v1/inventory/{}/decrease - amount={}", productId, request.getAmount());
        Inventory inventory = decreaseStockUseCase.decreaseStock(productId, request.getAmount());
        return ResponseEntity.ok(inventoryMapper.toResponse(inventory));
    }

    @PutMapping("/{productId}/increase")
    @CacheEvict(value = INVENTORY_CACHE, key = "#productId")
    public ResponseEntity<InventoryResponse> increase(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("PUT /api/v1/inventory/{}/increase - amount={}", productId, request.getAmount());
        Inventory inventory = increaseStockUseCase.increaseStock(productId, request.getAmount());
        return ResponseEntity.ok(inventoryMapper.toResponse(inventory));
    }
}
