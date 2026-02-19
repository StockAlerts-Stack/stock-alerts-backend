package com.stockalert.product.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STOCK_EVENTS_EXCHANGE = "stock.events";
    public static final String PRODUCT_STOCK_QUEUE = "product.stock.queue";
    public static final String PRODUCT_STOCK_ROUTING_KEY = "product.stock.changed";

    @Bean
    public TopicExchange stockEventsExchange() {
        return ExchangeBuilder.topicExchange(STOCK_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue productStockQueue() {
        return QueueBuilder.durable(PRODUCT_STOCK_QUEUE).build();
    }

    @Bean
    public Binding productStockBinding(Queue productStockQueue, TopicExchange stockEventsExchange) {
        return BindingBuilder.bind(productStockQueue).to(stockEventsExchange).with(PRODUCT_STOCK_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
