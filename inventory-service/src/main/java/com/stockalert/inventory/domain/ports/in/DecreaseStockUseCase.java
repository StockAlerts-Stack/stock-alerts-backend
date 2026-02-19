package com.stockalert.inventory.domain.ports.in;

import com.stockalert.inventory.domain.model.Inventory;

public interface DecreaseStockUseCase {

    Inventory decreaseStock(Long productId, Integer amount);
}
