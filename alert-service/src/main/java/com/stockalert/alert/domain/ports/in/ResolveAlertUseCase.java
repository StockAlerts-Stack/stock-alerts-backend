package com.stockalert.alert.domain.ports.in;

import com.stockalert.alert.domain.model.Alert;

public interface ResolveAlertUseCase {

    Alert resolveAlert(Long alertId);
}
