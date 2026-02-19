package com.stockalert.alert.domain.service;

import com.stockalert.alert.domain.exception.AlertNotFoundException;
import com.stockalert.alert.domain.model.Alert;
import com.stockalert.alert.domain.model.AlertStatus;
import com.stockalert.alert.domain.model.StockLowEvent;
import com.stockalert.alert.domain.ports.in.CreateAlertUseCase;
import com.stockalert.alert.domain.ports.in.GetAlertsUseCase;
import com.stockalert.alert.domain.ports.in.ResolveAlertUseCase;
import com.stockalert.alert.domain.ports.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AlertDomainService implements CreateAlertUseCase, GetAlertsUseCase, ResolveAlertUseCase {

    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public Alert createFromEvent(StockLowEvent event) {
        Alert alert = Alert.create(event.getProductId(), event.getCurrentStock(), event.getThreshold());
        Alert saved = alertRepositoryPort.save(alert);
        log.info("Alert created: id={}, productId={}, stock={}", saved.getId(), saved.getProductId(), saved.getCurrentStock());
        return saved;
    }

    @Override
    public List<Alert> getAllAlerts() {
        return alertRepositoryPort.findAll();
    }

    @Override
    public List<Alert> getActiveAlerts() {
        return alertRepositoryPort.findByStatus(AlertStatus.ACTIVE);
    }

    @Override
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepositoryPort.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));

        if (!alert.isActive()) {
            throw new IllegalStateException("Alert id=" + alertId + " is already resolved");
        }

        Alert resolved = alertRepositoryPort.save(alert.resolve());
        log.info("Alert resolved: id={}", resolved.getId());
        return resolved;
    }
}
