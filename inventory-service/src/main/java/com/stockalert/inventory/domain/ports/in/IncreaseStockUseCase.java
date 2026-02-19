package com.stockalert.inventory.domain.ports.in;

import com.stockalert.inventory.domain.model.Inventory;

public interface IncreaseStockUseCase {

    Inventory increaseStock(Long productId, Integer amount);
}
