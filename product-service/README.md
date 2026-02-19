# Product Service

A Spring Boot 3 microservice built using **Hexagonal Architecture** (Ports and Adapters pattern).

**Package:** `com.stockalert.product`

## Architecture Overview

This service follows clean architecture principles with clear separation between business logic and infrastructure:

```
├── domain/                          # Business Logic (No framework dependencies)
│   ├── model/                       # Domain entities (pure Java)
│   │   ├── Product                  # Product domain model
│   │   └── ProductStockChangedEvent # Domain event for stock changes
│   ├── ports/
│   │   ├── in/                      # Input ports (Use Cases)
│   │   │   ├── CreateProductUseCase
│   │   │   ├── UpdateProductUseCase
│   │   │   └── GetProductUseCase
│   │   └── out/                     # Output ports (Repository & Events)
│   │       ├── ProductRepositoryPort
│   │       └── ProductEventPublisherPort
│   └── service/                     # Use case implementations
│       └── ProductService
│
└── infrastructure/                  # Framework and External Adapters
    ├── adapters/
    │   ├── in/web/                  # REST API (Input Adapter)
    │   │   ├── ProductController
    │   │   ├── dto/                 # Request/Response DTOs
    │   │   ├── mapper/              # DTO to Domain mappers
    │   │   └── exception/           # Exception handlers
    │   └── out/
    │       ├── persistence/         # Database (Output Adapter)
    │       │   ├── entity/          # JPA entities
    │       │   ├── repository/      # Spring Data repositories
    │       │   ├── adapter/         # Repository port implementation
    │       │   └── mapper/          # Entity to Domain mappers
    │       └── messaging/           # Event Publishing (Output Adapter)
    │           └── ProductEventPublisherStub # Event publisher implementation
    └── config/                      # Spring configuration
        └── BeanConfiguration        # Domain service bean definitions
```

## Domain Model

### Product
- `id` (Long): Unique identifier
- `sku` (String): Stock Keeping Unit (unique)
- `name` (String): Product name
- `price` (BigDecimal): Product price
- `stock` (Integer): Available stock quantity

### ProductStockChangedEvent
Domain event published when product stock changes:
- `productId` (Long): Product identifier
- `sku` (String): Product SKU
- `name` (String): Product name
- `oldStock` (int): Previous stock quantity
- `newStock` (int): New stock quantity
- `timestamp` (LocalDateTime): Event timestamp
- `eventType` (EventType): PRODUCT_CREATED | STOCK_INCREASED | STOCK_DECREASED | STOCK_UPDATED

## Event-Driven Architecture

This service publishes domain events to notify other microservices about product changes:

**Published Events:**
- `PRODUCT_CREATED`: When a new product is created
- `STOCK_INCREASED`: When product stock increases
- `STOCK_DECREASED`: When product stock decreases
- `STOCK_UPDATED`: When stock changes but remains the same value

**Implementation:**
- **Production**: `RabbitMQStockPublisherAdapter` (Default - publishes to RabbitMQ)
- **Development/Testing**: `ProductEventPublisherStub` (Conditional - logs to console)
- **Configuration**: Switch implementations via `messaging.stub.enabled` property
- **Events**: Automatically published on create/update operations

**Event Flow (Production):**
```
ProductController → ProductService → ProductEventPublisherPort → RabbitMQStockPublisherAdapter
                         ↓                                              ↓
                   Domain Logic                              RabbitMQ Exchange (stock.events)
                                                                  ↓
                                                        Consuming Services
                                                        (alert-service, notification-service)
```

**RabbitMQ Configuration:**
- **Exchange**: `stock.events` (TopicExchange)
- **Queue**: `product.stock.queue`
- **Routing Key**: `product.stock.changed`
- **Connection**: Configured via environment variables

**Environment Variables:**
```bash
SPRING_RABBITMQ_HOST=localhost      # RabbitMQ host
SPRING_RABBITMQ_PORT=5672           # RabbitMQ port
SPRING_RABBITMQ_USERNAME=guest      # RabbitMQ username
SPRING_RABBITMQ_PASSWORD=guest      # RabbitMQ password
```

See [EVENTS.md](EVENTS.md) for complete event architecture documentation.

## API Endpoints

### Create Product
```http
POST /api/v1/products
Content-Type: application/json

{
  "sku": "PROD-001",
  "name": "Sample Product",
  "price": 99.99,
  "stock": 100
}
```

### Update Product
```http
PUT /api/v1/products/{id}
Content-Type: application/json

{
  "sku": "PROD-001",
  "name": "Updated Product",
  "price": 89.99,
  "stock": 150
}
```

### Get Product by ID
```http
GET /api/v1/products/{id}
```

### Get All Products
```http
GET /api/v1/products
```

### Get Product by SKU
```http
GET /api/v1/products/sku/{sku}
```

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.10**
  - Spring Web
  - Spring Data JPA
  - Spring Validation
  - Spring Actuator
  - Spring AMQP (RabbitMQ)
- **PostgreSQL** - Database
- **RabbitMQ** - Message broker for event-driven architecture
- **Lombok** - Boilerplate code reduction
- **Micrometer + Prometheus** - Metrics and monitoring

## Configuration

### Database Configuration (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stock_db
    username: admin
    password: adminpassword
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### Monitoring Endpoints
- Health: `http://localhost:8081/actuator/health`
- Info: `http://localhost:8081/actuator/info`
- Prometheus Metrics: `http://localhost:8081/actuator/prometheus`

## Running the Application

### Prerequisites
- Java 17 or higher
- PostgreSQL database running on localhost:5432
- Database named `stock_db` created

### Using Maven
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The service will start on port **8081**.

### Using Docker

#### Build the Docker Image
```bash
docker build -t product-service:latest .
```

#### Run the Container
```bash
# Run with default settings
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/stock_db \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=adminpassword \
  product-service:latest

# Run with custom JVM settings
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/stock_db \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=adminpassword \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  product-service:latest
```

#### Docker Image Benefits
- **Multi-stage build**: Separates build dependencies from runtime
- **Small footprint**: ~150-200MB using Alpine JRE (vs ~500MB+ with full JDK)
- **Security**: Runs as non-root user (`appuser`)
- **Performance**: Optimized JVM memory settings (-Xmx384m, -Xms128m)
- **Signal handling**: Proper SIGTERM handling for graceful shutdown
- **Layer caching**: Maven dependencies cached separately for faster rebuilds

### Using Docker Compose (Recommended)

Docker Compose orchestrates the Product Service along with all required infrastructure services (PostgreSQL, RabbitMQ, Redis, Prometheus, Grafana).

#### Quick Start with Automation Scripts

```bash
# Start all services (infrastructure + product-service)
./start-services.sh

# Stop all services
./stop-services.sh
```

#### Manual Docker Compose Commands

```bash
# Start infrastructure services first
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml up -d

# Start product service
cd ../stock-alerts-backend/product-service
docker-compose up -d

# View logs
docker-compose logs -f product-service

# Stop services
docker-compose down
```

#### Service Endpoints (Docker Compose)

After starting with Docker Compose, access:

- **Product Service API**: http://localhost:8081/api/v1/products
- **Health Check**: http://localhost:8081/actuator/health
- **Prometheus Metrics**: http://localhost:8081/actuator/prometheus
- **PostgreSQL**: localhost:5432 (admin/adminpassword)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

See [DOCKER-COMPOSE.md](DOCKER-COMPOSE.md) for detailed Docker Compose documentation.

## Design Principles

### Hexagonal Architecture
- **Domain Layer**: Contains pure business logic with no framework dependencies
- **Ports**: Interfaces that define contracts between layers
  - **Input Ports**: Use case interfaces (CreateProduct, UpdateProduct, etc.)
  - **Output Ports**: Repository interfaces
- **Adapters**: Implementations that connect the domain to external systems
  - **Input Adapters**: REST Controllers
  - **Output Adapters**: JPA Repository implementations

### Clean Architecture Rules
1. **No Spring annotations in domain layer**
   - Domain services are configured as beans in `BeanConfiguration`
   - Domain layer remains framework-agnostic
   
2. **Separation of Models**
   - Domain Model: Pure Java (`Product`, `ProductStockChangedEvent`)
   - Persistence Entity: JPA annotated (`ProductEntity`)
   - DTOs: Request/Response objects (`ProductRequest`, `ProductResponse`)
   - Mappers handle conversions between layers

3. **Dependency Direction**
   - Dependencies point inward (Infrastructure → Domain)
   - Domain layer has no dependencies on infrastructure
   - Domain defines contracts (ports), infrastructure implements them (adapters)

4. **Event-Driven Integration**
   - Domain defines events and publisher contracts
   - Infrastructure provides adapter implementations
   - Easy to switch from stub to real message broker

## Observability

The service is configured with:
- **Actuator**: Provides health checks and operational information
- **Prometheus**: Metrics endpoint for monitoring
- **Configurable logging**: SQL queries are logged in development

## Testing

Run tests with:
```bash
./mvnw test
```

## Documentation

- [DOCKER.md](DOCKER.md) - Detailed Docker image documentation
- [DOCKER-COMPOSE.md](DOCKER-COMPOSE.md) - Docker Compose orchestration guide
- [QUICKSTART.md](QUICKSTART.md) - Quick start guide for new developers
- [EVENTS.md](EVENTS.md) - Event-driven architecture documentation
- [RABBITMQ.md](RABBITMQ.md) - RabbitMQ integration and usage guide

## Future Enhancements

- Add DELETE product endpoint
- Implement product search and filtering
- Add pagination for product listing
- Implement stock reservation logic
- Add integration tests
- Add API documentation with OpenAPI/Swagger
- Implement event replay and dead letter queue handling
- Add circuit breaker for RabbitMQ connection failures
- Implement retry mechanism with exponential backoff
