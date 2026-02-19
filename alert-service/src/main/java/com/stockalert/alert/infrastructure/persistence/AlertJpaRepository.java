package com.stockalert.alert.infrastructure.persistence;

import com.stockalert.alert.domain.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, Long> {

    List<AlertJpaEntity> findByStatus(AlertStatus status);
}
