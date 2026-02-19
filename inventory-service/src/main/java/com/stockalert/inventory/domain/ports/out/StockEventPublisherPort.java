package com.stockalert.inventory.domain.ports.out;

import com.stockalert.inventory.domain.model.StockLowEvent;

public interface StockEventPublisherPort {

    void publishStockLow(StockLowEvent event);
}
