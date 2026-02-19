package com.stockalert.alert.domain.ports.out;

import com.stockalert.alert.domain.model.Alert;
import com.stockalert.alert.domain.model.AlertStatus;

import java.util.List;
import java.util.Optional;

public interface AlertRepositoryPort {

    Alert save(Alert alert);

    Optional<Alert> findById(Long id);

    List<Alert> findAll();

    List<Alert> findByStatus(AlertStatus status);
}
