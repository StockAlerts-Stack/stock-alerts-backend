package com.stockalert.alert.domain.exception;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long alertId) {
        super("Alert not found for id=" + alertId);
    }
}
