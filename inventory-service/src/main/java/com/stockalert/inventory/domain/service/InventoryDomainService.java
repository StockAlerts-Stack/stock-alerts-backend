package com.stockalert.inventory.domain.service;

import com.stockalert.inventory.domain.model.Inventory;
import com.stockalert.inventory.domain.model.StockLowEvent;
import com.stockalert.inventory.domain.ports.in.CreateInventoryUseCase;
import com.stockalert.inventory.domain.ports.in.DecreaseStockUseCase;
import com.stockalert.inventory.domain.ports.in.GetInventoryUseCase;
import com.stockalert.inventory.domain.ports.in.IncreaseStockUseCase;
import com.stockalert.inventory.domain.exception.DuplicateInventoryException;
import com.stockalert.inventory.domain.exception.InventoryNotFoundException;
import com.stockalert.inventory.domain.ports.out.InventoryRepositoryPort;
import com.stockalert.inventory.domain.ports.out.StockEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class InventoryDomainService
        implements GetInventoryUseCase, CreateInventoryUseCase, DecreaseStockUseCase, IncreaseStockUseCase {

    private final InventoryRepositoryPort inventoryRepository;
    private final StockEventPublisherPort eventPublisher;

    @Override
    public Inventory getByProductId(Long productId) {
        log.debug("Fetching inventory for productId={}", productId);
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    @Override
    public Inventory create(Long productId, Integer quantity, Integer threshold) {
        log.info("Creating inventory entry for productId={}", productId);
        if (inventoryRepository.existsByProductId(productId)) {
            throw new DuplicateInventoryException(productId);
        }
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .threshold(threshold)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Inventory saved = inventoryRepository.save(inventory);
        checkAndPublishStockLow(saved);
        return saved;
    }

    @Override
    public Inventory decreaseStock(Long productId, Integer amount) {
        log.info("Decreasing stock by {} for productId={}", amount, productId);
        Inventory inventory = getByProductId(productId);
        inventory.decrease(amount);
        Inventory saved = inventoryRepository.save(inventory);
        checkAndPublishStockLow(saved);
        return saved;
    }

    @Override
    public Inventory increaseStock(Long productId, Integer amount) {
        log.info("Increasing stock by {} for productId={}", amount, productId);
        Inventory inventory = getByProductId(productId);
        inventory.increase(amount);
        return inventoryRepository.save(inventory);
    }

    private void checkAndPublishStockLow(Inventory inventory) {
        if (inventory.isStockLow()) {
            log.warn("Stock LOW detected for productId={}, quantity={}, threshold={}",
                    inventory.getProductId(), inventory.getQuantity(), inventory.getThreshold());
            StockLowEvent event = StockLowEvent.builder()
                    .productId(inventory.getProductId())
                    .currentStock(inventory.getQuantity())
                    .threshold(inventory.getThreshold())
                    .timestamp(LocalDateTime.now())
                    .build();
            eventPublisher.publishStockLow(event);
        }
    }
}
