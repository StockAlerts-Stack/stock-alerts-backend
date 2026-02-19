#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
INFRA_DIR="../../stock-alerts-infrastructure"
SERVICE_DIR="."
INFRA_COMPOSE="docker-compose.infra.yml"
SERVICE_COMPOSE="docker-compose.yml"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      Stock Alert - Product Service Startup            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Docker is not running${NC}"
    exit 1
fi

# Step 1: Start Infrastructure Services
echo -e "${YELLOW}🔧 Step 1: Starting Infrastructure Services...${NC}"
if [ -d "$INFRA_DIR" ]; then
    cd "$INFRA_DIR"
    docker-compose -f "$INFRA_COMPOSE" up -d
    echo -e "${GREEN}✅ Infrastructure services started${NC}"
    cd - > /dev/null
else
    echo -e "${RED}❌ Error: Infrastructure directory not found at $INFRA_DIR${NC}"
    exit 1
fi

# Step 2: Wait for PostgreSQL to be ready
echo -e "${YELLOW}⏳ Step 2: Waiting for PostgreSQL to be healthy...${NC}"
RETRY_COUNT=0
MAX_RETRIES=30
until docker exec postgres-db pg_isready -U admin -d stock_db > /dev/null 2>&1 || [ $RETRY_COUNT -eq $MAX_RETRIES ]; do
    echo -n "."
    sleep 2
    ((RETRY_COUNT++))
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo -e "\n${RED}❌ Error: PostgreSQL did not become healthy in time${NC}"
    exit 1
fi
echo -e "\n${GREEN}✅ PostgreSQL is healthy${NC}"

# Step 3: Build Product Service
echo -e "${YELLOW}🏗️  Step 3: Building Product Service...${NC}"
cd "$SERVICE_DIR"
docker-compose build
echo -e "${GREEN}✅ Product Service built successfully${NC}"

# Step 4: Start Product Service
echo -e "${YELLOW}🚀 Step 4: Starting Product Service...${NC}"
docker-compose up -d
echo -e "${GREEN}✅ Product Service started${NC}"

# Step 5: Wait for service to be healthy
echo -e "${YELLOW}⏳ Step 5: Waiting for Product Service to be healthy...${NC}"
RETRY_COUNT=0
MAX_RETRIES=30
until curl -sf http://localhost:8081/actuator/health > /dev/null 2>&1 || [ $RETRY_COUNT -eq $MAX_RETRIES ]; do
    echo -n "."
    sleep 2
    ((RETRY_COUNT++))
done

echo ""
if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo -e "${YELLOW}⚠️  Warning: Product Service did not become healthy in time${NC}"
    echo -e "${YELLOW}   Check logs with: docker-compose logs -f product-service${NC}"
else
    echo -e "${GREEN}✅ Product Service is healthy${NC}"
fi

# Summary
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║               🎉 All Services Started!                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}📊 Service Endpoints:${NC}"
echo -e "   • Product Service API:     ${BLUE}http://localhost:8081${NC}"
echo -e "   • Health Check:            ${BLUE}http://localhost:8081/actuator/health${NC}"
echo -e "   • Prometheus Metrics:      ${BLUE}http://localhost:8081/actuator/prometheus${NC}"
echo -e "   • API Documentation:       ${BLUE}http://localhost:8081/api/v1/products${NC}"
echo ""
echo -e "${GREEN}📈 Infrastructure Services:${NC}"
echo -e "   • PostgreSQL:              ${BLUE}localhost:5432${NC}"
echo -e "   • Redis:                   ${BLUE}localhost:6379${NC}"
echo -e "   • RabbitMQ:                ${BLUE}localhost:5672${NC}"
echo -e "   • RabbitMQ Management:     ${BLUE}http://localhost:15672${NC} (guest/guest)"
echo -e "   • Prometheus:              ${BLUE}http://localhost:9090${NC}"
echo -e "   • Grafana:                 ${BLUE}http://localhost:3000${NC} (admin/admin)"
echo ""
echo -e "${YELLOW}💡 Useful Commands:${NC}"
echo -e "   • View logs:               ${BLUE}docker-compose logs -f product-service${NC}"
echo -e "   • Check status:            ${BLUE}docker-compose ps${NC}"
echo -e "   • Stop services:           ${BLUE}docker-compose down${NC}"
echo -e "   • Restart service:         ${BLUE}docker-compose restart product-service${NC}"
echo ""
