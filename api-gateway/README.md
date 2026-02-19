# API Gateway

Spring Cloud Gateway serving as the single entry point for the Stock Alert microservices system.

## Routing Table

| External path | Forwarded to | Strip |
|---------------|-------------|-------|
| `/api/products/**` | `product-service:8081` | `/api` (1 segment) |
| `/api/inventory/**` | `inventory-service:8082` | `/api` (1 segment) |
| `/api/alerts/**` | `alert-service:8083` | `/api` (1 segment) |

**StripPrefix=1** removes the first path segment before forwarding.
Example: `GET /api/products/123` → product-service receives `GET /products/123`.

> **Note:** downstream controller base paths must start with `/products`, `/inventory`, `/alerts` respectively.

## Architecture

```
com.stockalert.gateway
├── ApiGatewayApplication.java
├── config/
│   ├── CorsConfig.java        # CorsWebFilter — allows localhost:3000 + 5173
│   └── SecurityConfig.java    # Placeholder for future OAuth2/JWT
├── filters/
│   ├── LoggingFilter.java     # Logs method, path, response status (INFO)
│   └── RequestIdFilter.java   # Injects X-Request-ID if absent
└── exception/
    └── GlobalErrorHandler.java  # JSON error responses for gateway faults
```

## Filters

### LoggingFilter
Logs every incoming request and outgoing response at `INFO` level:
```
Incoming request: GET /api/products/1 from /127.0.0.1:52312
Completed: GET /api/products/1 -> status=200 OK
```

### RequestIdFilter
Generates a `UUID` and injects it as `X-Request-ID` header on the request (forwarded downstream) and the response (returned to client). If the client already sends `X-Request-ID`, it is preserved.

## CORS

Allowed origins: `http://localhost:3000`, `http://localhost:5173`
Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

To add a production frontend domain, update `CorsConfig.java`:
```java
config.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "https://your-production-domain.com"
));
```

## Running Locally

```bash
./mvnw spring-boot:run
```

The gateway is available at `http://localhost:8080`.

## Docker

```bash
docker build -t api-gateway:latest .
docker compose up -d
```

## Kubernetes

```bash
kubectl create namespace stock-alert

kubectl apply -f k8s/deployment.yaml
```

The gateway is exposed on `NodePort 30080`:
```
http://<vps-ip>:30080/api/products/...
http://<vps-ip>:30080/api/inventory/...
http://<vps-ip>:30080/api/alerts/...
```

## Actuator

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Prometheus metrics |

## Future: OAuth2

`SecurityConfig.java` is prepared as a placeholder. To enable JWT validation:
1. Add `spring-boot-starter-oauth2-resource-server` to `pom.xml`
2. Add `@EnableWebFluxSecurity` to `SecurityConfig`
3. Configure `SecurityWebFilterChain` with `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`
4. Set `spring.security.oauth2.resourceserver.jwt.issuer-uri` in `application.yml`
