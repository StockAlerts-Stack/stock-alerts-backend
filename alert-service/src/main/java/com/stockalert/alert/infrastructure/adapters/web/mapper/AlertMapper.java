package com.stockalert.alert.infrastructure.adapters.web.mapper;

import com.stockalert.alert.domain.model.Alert;
import com.stockalert.alert.infrastructure.adapters.web.dto.AlertResponse;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .productId(alert.getProductId())
                .currentStock(alert.getCurrentStock())
                .threshold(alert.getThreshold())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
