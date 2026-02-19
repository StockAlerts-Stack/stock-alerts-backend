# RabbitMQ Integration Guide

Complete guide for RabbitMQ event publishing in the Product Service.

## Overview

The Product Service uses RabbitMQ to publish domain events when product stock changes. This enables event-driven communication with other microservices in the ecosystem.

## Architecture

### Components

```
┌─────────────────┐
│ ProductService  │ (Domain Layer)
│ (Business Logic)│
└────────┬────────┘
         │ publishes events via
         ↓
┌────────────────────────────┐
│ ProductEventPublisherPort  │ (Domain Port)
│ (Interface)                │
└────────┬───────────────────┘
         │ implemented by
         ↓
┌────────────────────────────────┐
│ RabbitMQStockPublisherAdapter │ (Infrastructure)
│ Uses RabbitTemplate            │
└────────┬───────────────────────┘
         │ sends to
         ↓
┌────────────────────────────┐
│ RabbitMQ Exchange          │
│ stock.events (Topic)       │
└────────┬───────────────────┘
         │ routes with key: product.stock.changed
         ↓
┌────────────────────────────┐
│ product.stock.queue        │
└────────────────────────────┘
         │
         ↓
┌────────────────────────────┐
│ Consuming Services         │
│ (alert-service, etc.)      │
└────────────────────────────┘
```

## Configuration

### RabbitMQ Settings

**application.yaml**:
```yaml
spring:
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST:localhost}
    port: ${SPRING_RABBITMQ_PORT:5672}
    username: ${SPRING_RABBITMQ_USERNAME:guest}
    password: ${SPRING_RABBITMQ_PASSWORD:guest}
```

### Environment Variables

Set these environment variables to configure RabbitMQ connection:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_RABBITMQ_HOST` | RabbitMQ server hostname | `localhost` |
| `SPRING_RABBITMQ_PORT` | RabbitMQ server port | `5672` |
| `SPRING_RABBITMQ_USERNAME` | RabbitMQ username | `guest` |
| `SPRING_RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |

### Exchange and Queue Configuration

Configured in `RabbitMQConfig.java`:

- **Exchange Name**: `stock.events`
- **Exchange Type**: TopicExchange (durable, non-auto-delete)
- **Queue Name**: `product.stock.queue`
- **Queue Type**: Durable (survives broker restart)
- **Routing Key**: `product.stock.changed`
- **Message Format**: JSON (Jackson2JsonMessageConverter)

## Running with RabbitMQ

### Option 1: Docker Compose (Recommended)

```bash
# Start infrastructure services (includes RabbitMQ)
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml up -d

# Start product-service
cd ../stock-alerts-backend/product-service
docker-compose up -d
```

Or use automation scripts:
```bash
./start-services.sh
```

**RabbitMQ Management UI**: http://localhost:15672
- Username: `guest`
- Password: `guest`

### Option 2: Local RabbitMQ

1. **Install RabbitMQ**:
   ```bash
   # Ubuntu/Debian
   sudo apt-get install rabbitmq-server
   
   # macOS
   brew install rabbitmq
   
   # Or use Docker
   docker run -d --name rabbitmq \
     -p 5672:5672 \
     -p 15672:15672 \
     rabbitmq:3-management
   ```

2. **Start RabbitMQ**:
   ```bash
   sudo service rabbitmq-server start  # Linux
   brew services start rabbitmq        # macOS
   ```

3. **Run Product Service**:
   ```bash
   ./mvnw spring-boot:run
   ```

### Option 3: Custom RabbitMQ Server

Set environment variables before running:

```bash
export SPRING_RABBITMQ_HOST=rabbitmq.example.com
export SPRING_RABBITMQ_PORT=5672
export SPRING_RABBITMQ_USERNAME=myuser
export SPRING_RABBITMQ_PASSWORD=mypassword

./mvnw spring-boot:run
```

## Using the Stub (No RabbitMQ Required)

For development/testing without RabbitMQ:

**application.yaml**:
```yaml
messaging:
  stub:
    enabled: true
```

Or set environment variable:
```bash
export MESSAGING_STUB_ENABLED=true
./mvnw spring-boot:run
```

The stub logs events to console instead of publishing to RabbitMQ.

## Testing Event Publishing

### 1. Verify RabbitMQ is Running

```bash
# Check RabbitMQ is accessible
curl -u guest:guest http://localhost:15672/api/overview

# Or check with docker
docker ps | grep rabbitmq
```

### 2. Create a Product (Publishes PRODUCT_CREATED Event)

```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "price": 99.99,
    "stock": 100
  }'
```

**Expected Log**:
```
Publishing product event to RabbitMQ: PRODUCT_CREATED - Product: Test Product (SKU: TEST-001), Stock: 0 → 100
Successfully published event: ProductStockChangedEvent(...)
```

### 3. Update Product Stock (Publishes STOCK_INCREASED/DECREASED Event)

```bash
curl -X PUT http://localhost:8081/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "price": 99.99,
    "stock": 50
  }'
```

**Expected Log**:
```
Publishing product event to RabbitMQ: STOCK_DECREASED - Product: Test Product (SKU: TEST-001), Stock: 100 → 50
Successfully published event: ProductStockChangedEvent(...)
```

### 4. Verify Events in RabbitMQ Management UI

1. Open http://localhost:15672
2. Login with `guest` / `guest`
3. Go to **Exchanges** → `stock.events`
4. Check **Bindings** section
5. Go to **Queues** → `product.stock.queue`
6. Click **Get Messages** to view published events

## Event Schema

### ProductStockChangedEvent

```json
{
  "productId": 1,
  "sku": "TEST-001",
  "name": "Test Product",
  "oldStock": 100,
  "newStock": 50,
  "timestamp": "2026-02-16T18:30:00",
  "eventType": "STOCK_DECREASED"
}
```

### Event Types

| Event Type | Description | When Published |
|------------|-------------|----------------|
| `PRODUCT_CREATED` | New product created | Product created with initial stock |
| `STOCK_INCREASED` | Stock quantity increased | Update increases stock value |
| `STOCK_DECREASED` | Stock quantity decreased | Update decreases stock value |
| `STOCK_UPDATED` | Stock changed (same value) | Update doesn't change stock |

## Creating a Consumer Service

Example consumer in another microservice:

### 1. Add Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 2. Create Event Model

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockChangedEvent {
    private Long productId;
    private String sku;
    private String name;
    private int oldStock;
    private int newStock;
    private LocalDateTime timestamp;
    private EventType eventType;
    
    public enum EventType {
        PRODUCT_CREATED, STOCK_INCREASED, STOCK_DECREASED, STOCK_UPDATED
    }
}
```

### 3. Create Listener

```java
@Component
@Slf4j
public class ProductStockEventListener {
    
    @RabbitListener(queues = "product.stock.queue")
    public void handleStockEvent(ProductStockChangedEvent event) {
        log.info("Received stock event: {} for product {}", 
            event.getEventType(), event.getSku());
        
        // Process the event
        switch (event.getEventType()) {
            case PRODUCT_CREATED:
                handleProductCreated(event);
                break;
            case STOCK_DECREASED:
                if (event.getNewStock() < 10) {
                    sendLowStockAlert(event);
                }
                break;
            // ... handle other types
        }
    }
    
    private void handleProductCreated(ProductStockChangedEvent event) {
        // Notify users about new product
    }
    
    private void sendLowStockAlert(ProductStockChangedEvent event) {
        // Send alert about low stock
    }
}
```

### 4. Configure RabbitMQ

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 5. Create Custom Queue (Optional)

If you want a dedicated queue for your service:

```java
@Configuration
public class AlertServiceRabbitMQConfig {
    
    @Bean
    public Queue alertServiceQueue() {
        return new Queue("alert.service.queue", true);
    }
    
    @Bean
    public Binding alertServiceBinding() {
        return BindingBuilder
            .bind(alertServiceQueue())
            .to(new TopicExchange("stock.events"))
            .with("product.stock.changed");
    }
}
```

Update the listener:
```java
@RabbitListener(queues = "alert.service.queue")
public void handleStockEvent(ProductStockChangedEvent event) {
    // Your logic
}
```

## Troubleshooting

### Events Not Published

**Check 1: RabbitMQ Connection**
```bash
# View application logs
docker-compose logs product-service | grep -i rabbit

# Expected: Connection established
# Not expected: Connection refused, Authentication failed
```

**Check 2: Environment Variables**
```bash
docker-compose exec product-service env | grep RABBITMQ
```

**Check 3: Verify Stub is Not Active**
```bash
# Check application.yaml - should NOT have:
messaging:
  stub:
    enabled: true
```

### Events Published but Not Received

**Check 1: Exchange Exists**
- Open http://localhost:15672/#/exchanges
- Verify `stock.events` exists

**Check 2: Queue Exists and is Bound**
- Open http://localhost:15672/#/queues
- Verify `product.stock.queue` exists
- Click on queue → Check **Bindings**
- Should show: `stock.events` → `product.stock.changed`

**Check 3: Consumer is Connected**
- On queue page, check **Consumers** section
- Should show active consumer(s)

**Check 4: Message Format**
- Verify consumer uses Jackson2JsonMessageConverter
- Event model matches the published event structure

### Connection Refused

```bash
# Verify RabbitMQ is running
docker ps | grep rabbitmq

# Check RabbitMQ logs
docker-compose logs rabbitmq-broker

# Verify port is accessible
telnet localhost 5672
```

### Authentication Failed

```bash
# Verify credentials match
docker-compose exec product-service env | grep RABBITMQ_USERNAME
docker-compose exec product-service env | grep RABBITMQ_PASSWORD

# Check RabbitMQ user exists
docker-compose exec rabbitmq-broker rabbitmqctl list_users
```

## Monitoring

### Application Logs

```bash
# View events being published
docker-compose logs -f product-service | grep "Publishing product event"

# View RabbitMQ connection status
docker-compose logs product-service | grep -i "rabbit"
```

### RabbitMQ Management UI

**Key Metrics to Monitor**:
1. **Exchanges → stock.events**:
   - Message rate in
   - Message rate out

2. **Queues → product.stock.queue**:
   - Messages ready
   - Messages unacknowledged
   - Consumer count

3. **Connections**:
   - Active connections
   - Channels

### Prometheus Metrics

If you have Prometheus configured:
```bash
# View RabbitMQ connection metrics
curl http://localhost:8081/actuator/prometheus | grep rabbit
```

## Best Practices

### 1. Error Handling

The current implementation logs errors but continues execution (fail-safe):

```java
try {
    rabbitTemplate.convertAndSend(exchange, routingKey, event);
} catch (AmqpException e) {
    log.error("Failed to publish event", e);
    // Event is lost but application continues
}
```

**Consider for Production**:
- Store failed events in database
- Implement retry with exponential backoff
- Use dead letter queue (DLQ)
- Send alerts on repeated failures

### 2. Message Durability

- Exchanges are durable (survive broker restart)
- Queues are durable (messages persist)
- Messages are persistent by default with JSON converter

### 3. Idempotency

Consumers should handle duplicate events:
```java
@RabbitListener(queues = "product.stock.queue")
public void handleStockEvent(ProductStockChangedEvent event) {
    // Check if already processed
    if (eventRepository.existsByProductIdAndTimestamp(
        event.getProductId(), event.getTimestamp())) {
        log.warn("Event already processed, skipping: {}", event);
        return;
    }
    
    // Process event
    processEvent(event);
    
    // Record as processed
    eventRepository.save(event);
}
```

### 4. Circuit Breaker

Consider adding resilience patterns:
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

### 5. Monitoring and Alerts

- Monitor queue depth (alert if > threshold)
- Monitor consumer lag
- Alert on repeated publishing failures
- Track event processing time

## Additional Resources

- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [EVENTS.md](EVENTS.md) - Complete event architecture guide
- [RabbitMQ Management Plugin](https://www.rabbitmq.com/management.html)

## Summary

✅ **Complete RabbitMQ Integration**
- Event publishing to `stock.events` exchange
- JSON message serialization
- Environment-based configuration
- Stub available for testing
- Error handling and logging

✅ **Production Ready**
- Durable exchanges and queues
- Persistent messages
- Connection pooling (CachingConnectionFactory)
- Proper error handling

✅ **Developer Friendly**
- Simple configuration
- Stub mode for local development
- Comprehensive logging
- Easy to extend and customize
