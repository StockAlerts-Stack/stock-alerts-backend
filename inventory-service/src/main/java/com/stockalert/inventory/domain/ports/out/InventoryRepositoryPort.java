package com.stockalert.inventory.domain.ports.out;

import com.stockalert.inventory.domain.model.Inventory;

import java.util.Optional;

public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    Optional<Inventory> findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
