# Event-Driven Architecture - Product Service

## Overview

The Product Service implements event-driven architecture following Hexagonal Architecture principles. When product stock changes, events are published to notify other services in the ecosystem.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                         │
│  ┌────────────────────────────────────────────────┐    │
│  │         ProductService (Business Logic)        │    │
│  │                                                 │    │
│  │  - Creates/Updates products                    │    │
│  │  - Publishes events via port                   │    │
│  └─────────────┬───────────────────────┬──────────┘    │
│                │                       │                │
│    ┌───────────▼──────────┐  ┌────────▼──────────┐    │
│    │ ProductRepositoryPort│  │ProductEventPublisher│   │
│    │      (Interface)     │  │  Port (Interface)   │   │
│    └───────────┬──────────┘  └────────┬──────────┘    │
└────────────────┼──────────────────────┼───────────────┘
                 │                      │
┌────────────────┼──────────────────────┼───────────────┐
│                │  INFRASTRUCTURE LAYER│                │
│    ┌───────────▼──────────┐  ┌────────▼──────────┐    │
│    │ ProductRepositoryAdapter│ ProductEventPublisher│   │
│    │   (JPA Implementation) │ Adapter (RabbitMQ/  │   │
│    │                        │  Kafka/etc.)        │   │
│    └────────────────────────┘  └──────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## Domain Components

### 1. ProductStockChangedEvent

Domain event representing a change in product stock.

**Location**: `domain.model.ProductStockChangedEvent`

**Fields**:
```java
- Long productId         // Product identifier
- String sku            // Stock Keeping Unit
- String name           // Product name
- Integer oldStock      // Previous stock quantity
- Integer newStock      // New stock quantity
- LocalDateTime timestamp // When the event occurred
- EventType eventType   // Type of change
```

**Event Types**:
- `PRODUCT_CREATED`: New product added to inventory
- `STOCK_UPDATED`: Stock changed (generic)
- `STOCK_INCREASED`: Stock quantity increased
- `STOCK_DECREASED`: Stock quantity decreased

**Helper Methods**:
```java
- getStockDifference(): Integer    // Returns newStock - oldStock
- isStockIncreased(): boolean      // Returns true if stock increased
- isStockDecreased(): boolean      // Returns true if stock decreased
```

### 2. ProductEventPublisherPort

Output port defining the contract for event publishing.

**Location**: `domain.ports.out.ProductEventPublisherPort`

**Methods**:
```java
void publishStockChangeEvent(ProductStockChangedEvent event);
void publishProductEvent(ProductStockChangedEvent event);
```

**Characteristics**:
- Framework-agnostic interface
- No implementation details in domain
- Can be implemented with any messaging system (RabbitMQ, Kafka, SNS, etc.)

### 3. ProductService Updates

The domain service now:
1. Injects `ProductEventPublisherPort` via constructor
2. Publishes events when:
   - A product is created → `PRODUCT_CREATED` event
   - Stock is updated → `STOCK_INCREASED` or `STOCK_DECREASED` event

## Infrastructure Implementation

### ProductEventPublisherStub (Current)

**Location**: `infrastructure.adapters.out.messaging.ProductEventPublisherStub`

**Purpose**: Development/testing adapter that logs events instead of publishing them.

**Usage**: 
- Automatically used when no RabbitMQ/Kafka is configured
- Logs events to console with detailed information
- Validates event data

**Example Log Output**:
```
📢 [EVENT PUBLISHED] Stock Change Event: { 
  type: PRODUCT_CREATED, 
  sku: 'PROD-001', 
  productId: 1, 
  oldStock: 0, 
  newStock: 100, 
  difference: 100, 
  timestamp: 2026-02-16T18:30:00 
}
```

## Event Flow

### 1. Product Creation

```
User Request → ProductController → CreateProductUseCase
                                         ↓
                                  ProductService
                                         ↓
                          1. Validate product data
                          2. Save to repository
                          3. Publish PRODUCT_CREATED event
                                         ↓
                              ProductEventPublisherPort
                                         ↓
                       ProductEventPublisherStub (logs event)
```

**Event Payload**:
```json
{
  "productId": 1,
  "sku": "PROD-001",
  "name": "Sample Product",
  "oldStock": 0,
  "newStock": 100,
  "timestamp": "2026-02-16T18:30:00",
  "eventType": "PRODUCT_CREATED"
}
```

### 2. Product Update (Stock Change)

```
User Request → ProductController → UpdateProductUseCase
                                         ↓
                                  ProductService
                                         ↓
                          1. Fetch existing product
                          2. Capture old stock value
                          3. Update product fields
                          4. Save to repository
                          5. If stock changed → Publish event
                                         ↓
                              ProductEventPublisherPort
                                         ↓
                       ProductEventPublisherStub (logs event)
```

**Event Payload** (Stock Increase):
```json
{
  "productId": 1,
  "sku": "PROD-001",
  "name": "Sample Product",
  "oldStock": 100,
  "newStock": 150,
  "timestamp": "2026-02-16T18:35:00",
  "eventType": "STOCK_INCREASED"
}
```

**Event Payload** (Stock Decrease):
```json
{
  "productId": 1,
  "sku": "PROD-001",
  "name": "Sample Product",
  "oldStock": 150,
  "newStock": 75,
  "timestamp": "2026-02-16T18:40:00",
  "eventType": "STOCK_DECREASED"
}
```

## Testing Events

### 1. Create a Product

```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "name": "Test Product",
    "price": 99.99,
    "stock": 100
  }'
```

**Expected Event**:
```
📢 [EVENT PUBLISHED] Stock Change Event: { 
  type: PRODUCT_CREATED, 
  sku: 'PROD-001', 
  oldStock: 0, 
  newStock: 100, 
  difference: 100 
}
```

### 2. Increase Stock

```bash
curl -X PUT http://localhost:8081/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "name": "Test Product",
    "price": 99.99,
    "stock": 200
  }'
```

**Expected Event**:
```
📢 [EVENT PUBLISHED] Stock Change Event: { 
  type: STOCK_INCREASED, 
  sku: 'PROD-001', 
  oldStock: 100, 
  newStock: 200, 
  difference: 100 
}
```

### 3. Decrease Stock

```bash
curl -X PUT http://localhost:8081/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "name": "Test Product",
    "price": 99.99,
    "stock": 50
  }'
```

**Expected Event**:
```
📢 [EVENT PUBLISHED] Stock Change Event: { 
  type: STOCK_DECREASED, 
  sku: 'PROD-001', 
  oldStock: 200, 
  newStock: 50, 
  difference: -150 
}
```

## Viewing Events

Start the service and check the logs:

```bash
# Using Docker Compose
docker-compose logs -f product-service

# Using Maven
./mvnw spring-boot:run
```

Look for lines with `📢 [EVENT PUBLISHED]`

## RabbitMQ Implementation

The product-service includes a complete RabbitMQ adapter for event publishing.

### Implementation Overview

**Current Status**: ✅ Fully Implemented

The service now has two event publisher implementations:
- **RabbitMQStockPublisherAdapter** (Default - `@Primary`)
- **ProductEventPublisherStub** (Conditional - enabled via property)

### Architecture

**RabbitMQStockPublisherAdapter.java**:
```java
@Primary
@Component
public class RabbitMQStockPublisherAdapter implements ProductEventPublisherPort {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void publishProductEvent(ProductStockChangedEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.STOCK_EVENTS_EXCHANGE,
            RabbitMQConfig.PRODUCT_STOCK_ROUTING_KEY,
            event
        );
    }
}
```

### Configuration

**RabbitMQConfig.java** defines:
- **Exchange**: `stock.events` (TopicExchange)
- **Queue**: `product.stock.queue` (Durable)
- **Routing Key**: `product.stock.changed`
- **Message Converter**: Jackson2JsonMessageConverter (JSON serialization)

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

Configure RabbitMQ connection using:
- `SPRING_RABBITMQ_HOST` (default: localhost)
- `SPRING_RABBITMQ_PORT` (default: 5672)
- `SPRING_RABBITMQ_USERNAME` (default: guest)
- `SPRING_RABBITMQ_PASSWORD` (default: guest)

### Switching Between Implementations

**Use RabbitMQ (Default)**:
```yaml
# No additional configuration needed
# RabbitMQ adapter is @Primary and active by default
```

**Use Stub for Testing**:
```yaml
messaging:
  stub:
    enabled: true
```

Or as environment variable:
```bash
MESSAGING_STUB_ENABLED=true
```

### Running with Docker Compose

The docker-compose.yml includes RabbitMQ service:
```yaml
services:
  product-service:
    environment:
      SPRING_RABBITMQ_HOST: rabbitmq-broker
      SPRING_RABBITMQ_PORT: 5672
      SPRING_RABBITMQ_USERNAME: guest
      SPRING_RABBITMQ_PASSWORD: guest
```

RabbitMQ is already configured in the infrastructure:
```bash
# Start all services including RabbitMQ
./start-services.sh

# RabbitMQ Management UI available at:
# http://localhost:15672 (guest/guest)
```

## Integration with Other Services

Other microservices can subscribe to these events:

### Alert Service Example

```java
@Component
public class StockAlertListener {
    
    @RabbitListener(queues = "stock-changed-queue")
    public void handleStockChange(ProductStockChangedEvent event) {
        if (event.isStockDecreased() && event.getNewStock() < 10) {
            // Send low stock alert
            sendAlert("Low stock for " + event.getSku());
        }
    }
}
```

### Notification Service Example

```java
@Component
public class StockNotificationListener {
    
    @RabbitListener(queues = "stock-notification-queue")
    public void handleStockChange(ProductStockChangedEvent event) {
        if (event.getEventType() == EventType.PRODUCT_CREATED) {
            // Notify about new product
            notifyUsers("New product available: " + event.getName());
        }
    }
}
```

## Benefits

### 1. Loose Coupling
- Services don't need to know about each other
- Product service doesn't know who consumes events
- Easy to add new consumers without changing producer

### 2. Scalability
- Asynchronous communication
- Services can scale independently
- Message broker handles load distribution

### 3. Reliability
- Events are persisted in message broker
- Guaranteed delivery with acknowledgments
- Retry mechanisms for failed processing

### 4. Clean Architecture
- Domain layer defines contracts (ports)
- Infrastructure provides implementations
- Business logic remains framework-agnostic
- Easy to swap messaging systems

## Monitoring

### Event Publishing Metrics (Future)

With Micrometer and Prometheus:

```java
@Component
public class ProductEventPublisherMetrics implements ProductEventPublisherPort {
    
    private final ProductEventPublisherPort delegate;
    private final Counter eventsPublished;
    
    @Override
    public void publishStockChangeEvent(ProductStockChangedEvent event) {
        delegate.publishStockChangeEvent(event);
        eventsPublished.increment();
    }
}
```

### Prometheus Queries

```promql
# Events published per second
rate(product_events_published_total[5m])

# Events by type
sum by (event_type) (product_events_published_total)
```

## Best Practices

1. **Idempotency**: Events should include unique identifiers
2. **Versioning**: Include schema version for backward compatibility
3. **Error Handling**: Log failures but don't break the main flow
4. **Monitoring**: Track event publishing success/failure rates
5. **Testing**: Use test profiles with stub implementations
6. **Documentation**: Keep event schemas documented and versioned

## Troubleshooting

### Events Not Being Published

1. Check logs for errors
2. Verify `ProductEventPublisherPort` is properly injected
3. Ensure stock actually changed (old != new)
4. Check if stub is being used vs. real implementation

### Events Published but Not Received

1. Verify RabbitMQ connection
2. Check exchange and queue bindings
3. Verify routing keys match
4. Check consumer is running
5. Look for errors in consumer logs

## Summary

The Product Service implements complete event-driven architecture with RabbitMQ integration:

- ✅ Domain defines event structure (`ProductStockChangedEvent`)
- ✅ Domain defines publishing contract (`ProductEventPublisherPort`)
- ✅ Domain service publishes events on stock changes
- ✅ **Infrastructure provides RabbitMQ implementation (`RabbitMQStockPublisherAdapter`)**
- ✅ **Fully configured with exchange, queue, and bindings**
- ✅ **Environment-based RabbitMQ connection settings**
- ✅ Stub implementation available for testing (`messaging.stub.enabled=true`)
- ✅ JSON message serialization with Jackson
- ✅ Other services can subscribe to `stock.events` exchange
- ✅ Loose coupling and high scalability

### What's Included

**Production Ready**:
- RabbitMQStockPublisherAdapter (Default, `@Primary`)
- RabbitMQConfig with TopicExchange, Queue, and Binding
- JSON message converter
- Error handling and logging
- Environment variable configuration

**Development/Testing**:
- ProductEventPublisherStub (Conditional)
- Activates with `messaging.stub.enabled=true`
- No message broker required for local development

The implementation follows hexagonal architecture principles - domain remains pure, infrastructure provides multiple adapter implementations, and switching between them requires only configuration changes.
