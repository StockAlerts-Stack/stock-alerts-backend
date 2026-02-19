# Docker Compose Setup Guide

## Overview

The Product Service uses Docker Compose to orchestrate local development and testing environments. It connects to shared infrastructure services (PostgreSQL, RabbitMQ, Redis) via a common Docker network.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              stock-alert-network (bridge)               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐    ┌──────────────┐                 │
│  │  postgres-db │    │ redis-cache  │                 │
│  │  (port 5432) │    │ (port 6379)  │                 │
│  └──────────────┘    └──────────────┘                 │
│                                                         │
│  ┌──────────────┐    ┌──────────────┐                 │
│  │ rabbitmq-    │    │ prometheus   │                 │
│  │ broker       │    │ (port 9090)  │                 │
│  │ (5672/15672) │    └──────────────┘                 │
│  └──────────────┘                                      │
│                      ┌──────────────┐                 │
│                      │   grafana    │                 │
│                      │ (port 3000)  │                 │
│                      └──────────────┘                 │
│                                                         │
│  ┌────────────────────────────────────────┐           │
│  │       product-service                  │           │
│  │       (port 8081)                      │           │
│  └────────────────────────────────────────┘           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## Prerequisites

1. Docker Engine 20.10+ installed
2. Docker Compose 1.29+ installed
3. Infrastructure services running (postgres-db, redis-cache, rabbitmq-broker)

## Quick Start

### Step 1: Start Infrastructure Services

First, start the infrastructure services from the `stock-alerts-infrastructure` directory:

```bash
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml up -d
```

**Verify infrastructure is running:**
```bash
docker ps

# Expected output:
# - postgres-db (healthy)
# - redis-cache
# - rabbitmq-broker
# - prometheus
# - grafana
```

### Step 2: Start Product Service

From the `product-service` directory:

```bash
cd ../stock-alerts-backend/product-service
docker-compose up -d
```

**Monitor startup logs:**
```bash
docker-compose logs -f product-service
```

### Step 3: Verify Service Health

```bash
# Check health status
curl http://localhost:8081/actuator/health

# Expected response:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" },
#     "ping": { "status": "UP" }
#   }
# }
```

## Docker Compose Configuration

### Service Configuration

| Setting | Value | Description |
|---------|-------|-------------|
| **Container Name** | `product-service` | Fixed name for consistent networking |
| **Port Mapping** | `8081:8081` | Exposes API on localhost:8081 |
| **Network** | `stock-alert-network` | Shared bridge network with infrastructure |
| **Memory Limit** | 600MB | Maximum memory allocation |
| **CPU Limit** | 1.0 cores | Maximum CPU allocation |
| **Restart Policy** | `unless-stopped` | Auto-restart except when manually stopped |

### Environment Variables

| Variable | Value | Purpose |
|----------|-------|---------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres-db:5432/stock_db` | Database connection string |
| `SPRING_DATASOURCE_USERNAME` | `admin` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `adminpassword` | Database password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Auto-update database schema |
| `SPRING_JPA_SHOW_SQL` | `true` | Log SQL queries (dev mode) |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | `health,info,prometheus` | Exposed actuator endpoints |

### Health Check Configuration

```yaml
healthcheck:
  test: wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health
  interval: 30s        # Check every 30 seconds
  timeout: 10s         # Wait up to 10 seconds for response
  retries: 5           # Retry 5 times before marking unhealthy
  start_period: 60s    # Grace period on startup
```

### Dependency Management

The service waits for PostgreSQL to be healthy before starting:

```yaml
depends_on:
  postgres-db:
    condition: service_healthy
```

## Common Commands

### Build and Start
```bash
# Build and start in detached mode
docker-compose up -d --build

# Build without cache
docker-compose build --no-cache
docker-compose up -d
```

### View Logs
```bash
# Follow logs
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100

# Specific service
docker-compose logs -f product-service
```

### Stop and Remove
```bash
# Stop service (keeps container)
docker-compose stop

# Stop and remove containers
docker-compose down

# Remove containers, networks, and volumes
docker-compose down -v
```

### Restart Service
```bash
# Restart specific service
docker-compose restart product-service

# Full restart (stop + start)
docker-compose down && docker-compose up -d
```

### Execute Commands in Container
```bash
# Open shell
docker-compose exec product-service sh

# Check Java version
docker-compose exec product-service java -version

# View environment variables
docker-compose exec product-service env | grep SPRING
```

### Resource Monitoring
```bash
# View resource usage
docker stats product-service

# View detailed container info
docker-compose ps
docker inspect product-service
```

## Network Configuration

### Shared Network: `stock-alert-network`

The network is created by the infrastructure docker-compose and shared with microservices.

**Network details:**
```bash
# Inspect network
docker network inspect stock-alert-network

# List connected containers
docker network inspect stock-alert-network -f '{{range .Containers}}{{.Name}} {{end}}'
```

**Service Discovery:**
- Services communicate using container names
- Example: `postgres-db:5432` resolves to PostgreSQL container
- DNS resolution handled by Docker's embedded DNS server

## Troubleshooting

### Service won't start

**Issue:** Container exits immediately
```bash
# Check logs for errors
docker-compose logs product-service

# Common causes:
# - Database not ready → Wait for postgres-db to be healthy
# - Connection refused → Verify network connectivity
# - Port conflict → Check if 8081 is already in use
```

### Database connection errors

**Issue:** `Connection refused` or `Unknown host`
```bash
# Verify postgres-db is running
docker ps | grep postgres-db

# Verify network connectivity
docker-compose exec product-service ping postgres-db

# Check database logs
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml logs postgres-db
```

### Health check failing

**Issue:** Service shows as unhealthy
```bash
# Manual health check
curl http://localhost:8081/actuator/health

# Check if application is listening
docker-compose exec product-service netstat -tulpn | grep 8081

# View startup logs
docker-compose logs --tail=200 product-service
```

### Out of memory

**Issue:** Container is killed by OOM
```bash
# Check resource usage
docker stats product-service

# Increase memory limit in docker-compose.yml:
# deploy.resources.limits.memory: 800M

# Adjust JVM settings in Dockerfile:
# -Xmx512m -Xms256m
```

### Port already in use

**Issue:** `Bind for 0.0.0.0:8081 failed: port is already allocated`
```bash
# Find process using port 8081
lsof -i :8081
# OR on Linux:
netstat -tulpn | grep 8081

# Kill the process or change port in docker-compose.yml:
# ports: - "8082:8081"
```

## Development Workflow

### Local Development with Hot Reload

For development with live code reload, you can mount source code as a volume:

```yaml
# Add to docker-compose.override.yml (not tracked in git)
services:
  product-service:
    volumes:
      - ./target/demo-0.0.1-SNAPSHOT.jar:/app/app.jar
    environment:
      SPRING_DEVTOOLS_RESTART_ENABLED: "true"
```

### Running Tests in Container

```bash
# Run Maven tests inside container
docker-compose run --rm product-service sh -c "cd /build && mvn test"
```

### Debugging

```bash
# Add debug port to docker-compose.yml
ports:
  - "8081:8081"
  - "5005:5005"  # Debug port

# Update ENTRYPOINT in Dockerfile
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", ...]
```

## Production Considerations

### Environment-Specific Overrides

Create `docker-compose.prod.yml`:
```yaml
version: '3.8'
services:
  product-service:
    environment:
      SPRING_JPA_SHOW_SQL: "false"
      SPRING_PROFILES_ACTIVE: prod
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: '2.0'
```

Run with:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Secrets Management

Never commit sensitive data. Use Docker secrets or environment files:

```bash
# Create .env file (add to .gitignore)
SPRING_DATASOURCE_PASSWORD=secure_password_here

# Reference in docker-compose.yml
environment:
  SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
```

### Monitoring

Access monitoring dashboards:
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

## Full Stack Startup Script

Create `start-all.sh`:
```bash
#!/bin/bash
set -e

echo "🚀 Starting Stock Alert infrastructure..."
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml up -d

echo "⏳ Waiting for infrastructure to be ready..."
sleep 10

echo "🔧 Starting Product Service..."
cd ../stock-alerts-backend/product-service
docker-compose up -d

echo "✅ All services started successfully!"
echo "📊 Product Service: http://localhost:8081"
echo "📈 Prometheus: http://localhost:9090"
echo "📊 Grafana: http://localhost:3000"
echo "🐰 RabbitMQ: http://localhost:15672"
```

Make executable: `chmod +x start-all.sh`

## Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Docker Network Guide](https://docs.docker.com/network/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Main Docker Documentation](./DOCKER.md)
