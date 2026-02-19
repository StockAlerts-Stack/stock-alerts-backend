package com.stockalert.inventory.domain.exception;

public class DuplicateInventoryException extends RuntimeException {

    public DuplicateInventoryException(Long productId) {
        super("Inventory already exists for productId=" + productId);
    }
}
