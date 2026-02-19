package com.stockalert.inventory.infrastructure.messaging;

import com.stockalert.inventory.domain.model.StockLowEvent;
import com.stockalert.inventory.domain.ports.out.StockEventPublisherPort;
import com.stockalert.inventory.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements StockEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishStockLow(StockLowEvent event) {
        log.info("Publishing STOCK_LOW event for productId={}", event.getProductId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.STOCK_EVENTS_EXCHANGE,
                RabbitMQConfig.STOCK_LOW_ROUTING_KEY,
                event
        );
        log.debug("STOCK_LOW event published: {}", event);
    }
}
