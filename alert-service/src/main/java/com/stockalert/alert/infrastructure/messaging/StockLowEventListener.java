package com.stockalert.alert.infrastructure.messaging;

import com.stockalert.alert.domain.model.StockLowEvent;
import com.stockalert.alert.domain.ports.in.CreateAlertUseCase;
import com.stockalert.alert.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockLowEventListener {

    private final CreateAlertUseCase createAlertUseCase;

    @RabbitListener(queues = RabbitMQConfig.STOCK_LOW_QUEUE)
    public void onStockLowEvent(StockLowEvent event) {
        log.info("STOCK_LOW event received: productId={}, stock={}, threshold={}",
                event.getProductId(), event.getCurrentStock(), event.getThreshold());
        createAlertUseCase.createFromEvent(event);
    }
}
