package com.stockalert.alert.domain.ports.in;

import com.stockalert.alert.domain.model.Alert;
import com.stockalert.alert.domain.model.StockLowEvent;

public interface CreateAlertUseCase {

    Alert createFromEvent(StockLowEvent event);
}
