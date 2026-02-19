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
echo -e "${BLUE}║      Stock Alert - Stop All Services                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Step 1: Stop Product Service
echo -e "${YELLOW}🛑 Step 1: Stopping Product Service...${NC}"
cd "$SERVICE_DIR"
if [ -f "$SERVICE_COMPOSE" ]; then
    docker-compose down
    echo -e "${GREEN}✅ Product Service stopped${NC}"
else
    echo -e "${YELLOW}⚠️  Warning: docker-compose.yml not found${NC}"
fi

# Step 2: Stop Infrastructure Services (optional)
echo ""
read -p "$(echo -e ${YELLOW}Do you want to stop infrastructure services? [y/N]:${NC} )" -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}🛑 Step 2: Stopping Infrastructure Services...${NC}"
    if [ -d "$INFRA_DIR" ]; then
        cd "$INFRA_DIR"
        docker-compose -f "$INFRA_COMPOSE" down
        echo -e "${GREEN}✅ Infrastructure services stopped${NC}"
        cd - > /dev/null
    else
        echo -e "${RED}❌ Error: Infrastructure directory not found${NC}"
    fi
else
    echo -e "${BLUE}ℹ️  Infrastructure services kept running${NC}"
fi

# Summary
echo ""
echo -e "${GREEN}✅ Services stopped successfully${NC}"
echo ""
echo -e "${YELLOW}💡 To start again:${NC}"
echo -e "   ${BLUE}./start-services.sh${NC}"
echo ""
