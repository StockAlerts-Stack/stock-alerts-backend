package com.stockalert.inventory.infrastructure.persistence;

import com.stockalert.inventory.domain.model.Inventory;
import com.stockalert.inventory.domain.ports.out.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final InventoryJpaRepository jpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity entity = toEntity(inventory);
        InventoryJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).map(this::toDomain);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jpaRepository.existsByProductId(productId);
    }

    private InventoryJpaEntity toEntity(Inventory inventory) {
        return InventoryJpaEntity.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .threshold(inventory.getThreshold())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private Inventory toDomain(InventoryJpaEntity entity) {
        return Inventory.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .threshold(entity.getThreshold())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
