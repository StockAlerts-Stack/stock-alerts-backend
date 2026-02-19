# Quick Start Guide - Docker Compose

## 🚀 Fastest Way to Run Everything

### Option 1: Automated Script (Recommended)
```bash
cd stock-alerts-backend/product-service
./start-services.sh
```

This script will:
1. ✅ Start infrastructure services (PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana)
2. ✅ Wait for PostgreSQL to be ready
3. ✅ Build the Product Service Docker image
4. ✅ Start the Product Service
5. ✅ Verify health status

### Option 2: Manual Steps
```bash
# Step 1: Start infrastructure
cd stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml up -d

# Step 2: Wait a few seconds for PostgreSQL
sleep 10

# Step 3: Start product service
cd ../stock-alerts-backend/product-service
docker-compose up -d --build

# Step 4: Check status
docker-compose ps
```

## 📊 Access Points

After startup, access these URLs:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Product Service API** | http://localhost:8081/api/v1/products | - |
| **Health Check** | http://localhost:8081/actuator/health | - |
| **Prometheus Metrics** | http://localhost:8081/actuator/prometheus | - |
| **RabbitMQ Management** | http://localhost:15672 | guest/guest |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin/admin |

## 🧪 Test the API

### Create a Product
```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "name": "Sample Product",
    "price": 99.99,
    "stock": 100
  }'
```

### Get All Products
```bash
curl http://localhost:8081/api/v1/products
```

### Get Product by ID
```bash
curl http://localhost:8081/api/v1/products/1
```

## 🛑 Stop Everything

### Option 1: Using Script
```bash
./stop-services.sh
```

### Option 2: Manual
```bash
# Stop product service
docker-compose down

# Stop infrastructure (from infrastructure directory)
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml down
```

## 🔍 Troubleshooting

### Check Logs
```bash
# Product service logs
docker-compose logs -f product-service

# Infrastructure logs
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml logs -f postgres-db
```

### Check Health
```bash
# Product service health
curl http://localhost:8081/actuator/health

# PostgreSQL health
docker exec postgres-db pg_isready -U admin -d stock_db
```

### Rebuild from Scratch
```bash
# Stop and remove everything
docker-compose down -v
cd ../../stock-alerts-infrastructure
docker-compose -f docker-compose.infra.yml down -v

# Start fresh
cd ../stock-alerts-backend/product-service
./start-services.sh
```

## 📚 Full Documentation

- [DOCKER-COMPOSE.md](./DOCKER-COMPOSE.md) - Complete Docker Compose guide
- [DOCKER.md](./DOCKER.md) - Dockerfile and Alpine JRE benefits
- [README.md](./README.md) - Project overview and architecture
