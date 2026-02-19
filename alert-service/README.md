# Alert Service

Spring Boot 3 microservice that consumes `STOCK_LOW` events from RabbitMQ and exposes an alert management REST API.

## How It Works

1. **Inventory Service** publishes a `STOCK_LOW` event to RabbitMQ whenever `quantity <= threshold`.
2. **Alert Service** listens on the `inventory.stock.low.queue` queue.
3. On each event, a new `Alert` is persisted to PostgreSQL with `status = ACTIVE`.
4. REST endpoints allow querying and resolving alerts.

## Architecture

```
com.stockalert.alert
├── domain/
│   ├── model/          # Alert, AlertStatus, StockLowEvent (pure Java, no framework)
│   ├── exception/      # AlertNotFoundException
│   ├── ports/
│   │   ├── in/         # CreateAlertUseCase, GetAlertsUseCase, ResolveAlertUseCase
│   │   └── out/        # AlertRepositoryPort
│   └── service/        # AlertDomainService
├── infrastructure/
│   ├── persistence/    # JPA entity, Spring Data repository, adapter
│   ├── messaging/      # StockLowEventListener (RabbitMQ consumer)
│   ├── config/         # RabbitMQConfig, BeanConfiguration
│   └── adapters/
│       └── web/        # AlertController, DTOs, mapper, exception handler
└── AlertServiceApplication.java
```

## REST API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/alerts` | List all alerts |
| GET | `/api/v1/alerts/active` | List active alerts only |
| PUT | `/api/v1/alerts/{id}/resolve` | Resolve an alert |

## RabbitMQ

| Property | Value |
|----------|-------|
| Exchange | `stock.events` (Topic, durable) |
| Queue | `inventory.stock.low.queue` (durable) |
| Routing Key | `inventory.stock.low` |
| Listener threads | 1 |

## Configuration

All sensitive values are injected via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `alert_db` | Database name |
| `DB_USERNAME` | `admin` | Database user |
| `DB_PASSWORD` | `adminpassword` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ port |
| `RABBITMQ_USERNAME` | `guest` | RabbitMQ user |
| `RABBITMQ_PASSWORD` | `guest` | RabbitMQ password |

## Running Locally

```bash
# Start PostgreSQL only
docker compose up postgres -d

# Run the service
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t alert-service:latest .
docker compose up -d
```

## Kubernetes

```bash
# Create the namespace (if not exists)
kubectl create namespace stock-alert

# Create secrets
kubectl create secret generic alert-service-secrets \
  --from-literal=DB_USERNAME=admin \
  --from-literal=DB_PASSWORD=adminpassword \
  --from-literal=RABBITMQ_HOST=<rabbitmq-host> \
  --from-literal=RABBITMQ_USERNAME=<user> \
  --from-literal=RABBITMQ_PASSWORD=<password> \
  -n stock-alert

# Deploy
kubectl apply -f k8s/deployment.yaml
```

## Actuator

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Prometheus metrics |
