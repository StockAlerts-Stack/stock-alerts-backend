package com.stockalert.alert.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    private Long id;
    private Long productId;
    private int currentStock;
    private int threshold;
    private AlertStatus status;
    private LocalDateTime createdAt;

    public static Alert create(Long productId, int currentStock, int threshold) {
        return Alert.builder()
                .productId(productId)
                .currentStock(currentStock)
                .threshold(threshold)
                .status(AlertStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Alert resolve() {
        return Alert.builder()
                .id(this.id)
                .productId(this.productId)
                .currentStock(this.currentStock)
                .threshold(this.threshold)
                .status(AlertStatus.RESOLVED)
                .createdAt(this.createdAt)
                .build();
    }

    public boolean isActive() {
        return AlertStatus.ACTIVE.equals(this.status);
    }
}
