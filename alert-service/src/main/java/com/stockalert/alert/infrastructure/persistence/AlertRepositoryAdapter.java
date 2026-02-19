package com.stockalert.alert.infrastructure.persistence;

import com.stockalert.alert.domain.model.Alert;
import com.stockalert.alert.domain.model.AlertStatus;
import com.stockalert.alert.domain.ports.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AlertRepositoryAdapter implements AlertRepositoryPort {

    private final AlertJpaRepository jpaRepository;

    @Override
    public Alert save(Alert alert) {
        AlertJpaEntity entity = toEntity(alert);
        AlertJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Alert> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Alert> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Alert> findByStatus(AlertStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private AlertJpaEntity toEntity(Alert alert) {
        return AlertJpaEntity.builder()
                .id(alert.getId())
                .productId(alert.getProductId())
                .currentStock(alert.getCurrentStock())
                .threshold(alert.getThreshold())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private Alert toDomain(AlertJpaEntity entity) {
        return Alert.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .currentStock(entity.getCurrentStock())
                .threshold(entity.getThreshold())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
