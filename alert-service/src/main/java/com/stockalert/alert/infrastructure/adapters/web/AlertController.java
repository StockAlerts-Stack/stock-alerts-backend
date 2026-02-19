package com.stockalert.alert.infrastructure.adapters.web;

import com.stockalert.alert.domain.ports.in.GetAlertsUseCase;
import com.stockalert.alert.domain.ports.in.ResolveAlertUseCase;
import com.stockalert.alert.infrastructure.adapters.web.dto.AlertResponse;
import com.stockalert.alert.infrastructure.adapters.web.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final GetAlertsUseCase getAlertsUseCase;
    private final ResolveAlertUseCase resolveAlertUseCase;
    private final AlertMapper alertMapper;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        log.debug("GET /api/v1/alerts");
        List<AlertResponse> alerts = getAlertsUseCase.getAllAlerts().stream()
                .map(alertMapper::toResponse)
                .toList();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        log.debug("GET /api/v1/alerts/active");
        List<AlertResponse> alerts = getAlertsUseCase.getActiveAlerts().stream()
                .map(alertMapper::toResponse)
                .toList();
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(@PathVariable Long id) {
        log.info("PUT /api/v1/alerts/{}/resolve", id);
        AlertResponse response = alertMapper.toResponse(resolveAlertUseCase.resolveAlert(id));
        return ResponseEntity.ok(response);
    }
}
