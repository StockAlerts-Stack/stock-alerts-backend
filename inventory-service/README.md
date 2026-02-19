# Inventory Service

Spring Boot 3 microservice responsible for managing product stock levels within the **Stock Alert** platform.

---

## Architecture

This service follows **Hexagonal Architecture** (Ports & Adapters), ensuring complete isolation of business logic from infrastructure concerns.

```
com.stockalert.inventory
├── domain
│   ├── exception           # Domain-specific exceptions
│   ├── model               # Pure business entities (Inventory, StockLowEvent)
│   ├── ports
│   │   ├── in              # Inbound ports (use case interfaces)
│   │   └── out             # Outbound ports (repository, event publisher interfaces)
│   └── service             # InventoryDomainService — core business logic
└── infrastructure
    ├── adapters
    │   └── web             # InventoryController, DTOs, MapStruct mapper, GlobalExceptionHandler
    ├── config              # BeanConfiguration, RabbitMQConfig, CacheConfig
    ├── messaging           # RabbitMQEventPublisher (implements StockEventPublisherPort)
    └── persistence         # JPA entity, Spring Data repo, InventoryRepositoryAdapter
```

### How it works

1. A REST request hits `InventoryController` (input adapter).
2. The controller delegates to an inbound port (use case interface).
3. `InventoryDomainService` implements all use case interfaces and holds the business rules.
4. The domain service uses outbound ports (`InventoryRepositoryPort`, `StockEventPublisherPort`) — never knowing about JPA or RabbitMQ directly.
5. Infrastructure adapters (`InventoryRepositoryAdapter`, `RabbitMQEventPublisher`) implement these ports.
6. `BeanConfiguration` wires everything together via explicit `@Bean` declarations.

---

## Business Rules

| Operation       | Rule                                                          |
|-----------------|---------------------------------------------------------------|
| `decreaseStock` | Quantity must not go below 0 (throws `IllegalStateException`) |
| `increaseStock` | Amount must be > 0                                            |
| Stock low check | After every write, if `quantity <= threshold` → publish `STOCK_LOW` event |

---

## REST API

Base path: `/api/v1/inventory`

| Method | Endpoint                        | Description                  | Status |
|--------|---------------------------------|------------------------------|--------|
| GET    | `/{productId}`                  | Get inventory by product ID  | 200    |
| POST   | `/`                             | Create inventory entry       | 201    |
| PUT    | `/{productId}/decrease`         | Decrease stock quantity      | 200    |
| PUT    | `/{productId}/increase`         | Increase stock quantity      | 200    |

### Example Payloads

**POST /api/v1/inventory**
```json
{
  "productId": 1,
  "quantity": 100,
  "threshold": 10
}
```

**PUT /api/v1/inventory/1/decrease**
```json
{ "amount": 5 }
```

**Response**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 95,
  "threshold": 10,
  "stockLow": false,
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T10:05:00"
}
```

---

## Cache (Redis)

- `GET /{productId}` is cached under key `inventory::<productId>` with a 10-minute TTL.
- Cache is evicted on any `decrease` or `increase` operation via `@CacheEvict`.

---

## Messaging (RabbitMQ)

| Component     | Name                          |
|---------------|-------------------------------|
| Exchange      | `stock.events` (Topic)        |
| Queue         | `inventory.stock.low.queue`   |
| Routing Key   | `inventory.stock.low`         |

**STOCK_LOW event payload**
```json
{
  "productId": 1,
  "currentStock": 8,
  "threshold": 10,
  "timestamp": "2026-02-18T10:05:00"
}
```

---

## Observability

| Endpoint                  | Description                   |
|---------------------------|-------------------------------|
| `/actuator/health`        | Liveness and readiness probes |
| `/actuator/prometheus`    | Prometheus metrics scrape     |
| `/actuator/metrics`       | All registered metrics        |

---

## Running Locally

### Prerequisites
- Java 21
- Docker & Docker Compose

### With Docker Compose (full stack)

```bash
docker compose up --build
```

Service will be available at `http://localhost:8082`.

### Build only

```bash
./mvnw clean package -DskipTests
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar
```

---

## Environment Variables

| Variable                  | Default         | Description               |
|---------------------------|-----------------|---------------------------|
| `DB_HOST`                 | `localhost`     | PostgreSQL host           |
| `DB_PORT`                 | `5432`          | PostgreSQL port           |
| `DB_NAME`                 | `inventory_db`  | Database name             |
| `DB_USERNAME`             | `admin`         | Database user             |
| `DB_PASSWORD`             | `adminpassword` | Database password         |
| `SPRING_RABBITMQ_HOST`    | `localhost`     | RabbitMQ host             |
| `SPRING_RABBITMQ_PORT`    | `5672`          | RabbitMQ port             |
| `SPRING_RABBITMQ_USERNAME`| `guest`         | RabbitMQ user             |
| `SPRING_RABBITMQ_PASSWORD`| `guest`         | RabbitMQ password         |
| `REDIS_HOST`              | `localhost`     | Redis host                |
| `REDIS_PORT`              | `6379`          | Redis port                |

---

## Kubernetes Deployment

```bash
kubectl apply -f k8s/deployment.yaml
```

The manifest includes: `Deployment`, `Service` (ClusterIP), `ConfigMap`, and `HorizontalPodAutoscaler`.

Secrets (`inventory-db-secret`, `rabbitmq-secret`) must be created separately:

```bash
kubectl create secret generic inventory-db-secret \
  --from-literal=host=postgres-service \
  --from-literal=name=inventory_db \
  --from-literal=username=admin \
  --from-literal=password=<your-password> \
  -n stock-alert
```
