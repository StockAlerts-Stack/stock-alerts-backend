package com.stockalert.alert.domain.ports.in;

import com.stockalert.alert.domain.model.Alert;

import java.util.List;

public interface GetAlertsUseCase {

    List<Alert> getAllAlerts();

    List<Alert> getActiveAlerts();
}
