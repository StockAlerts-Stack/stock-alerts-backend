package com.stockalert.alert.infrastructure.adapters.web.dto;

import com.stockalert.alert.domain.model.AlertStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponse {

    private Long id;
    private Long productId;
    private int currentStock;
    private int threshold;
    private AlertStatus status;
    private LocalDateTime createdAt;
}
