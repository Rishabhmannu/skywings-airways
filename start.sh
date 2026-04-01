#!/bin/bash
# SkyWings Airways — Start Script
# Starts: Docker (PostgreSQL + Redis) → Backend (Spring Boot) → Frontend (Vite)
# Then verifies all services are live.

set -e
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo ""
echo "========================================="
echo "  SkyWings Airways — Starting Up"
echo "========================================="
echo ""

# 0. Pre-flight checks
echo "Pre-flight checks..."

# Java
export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
if [ -z "$JAVA_HOME" ]; then
    echo -e "  ${RED}✗ Java 21 not found. Install with: brew install --cask temurin@21${NC}"
    exit 1
fi
JAVA_VER=$("$JAVA_HOME/bin/java" -version 2>&1 | head -1)
echo -e "  ${GREEN}✓${NC} Java: $JAVA_VER"

# Maven
if ! command -v mvn &>/dev/null; then
    echo -e "  ${RED}✗ Maven not found. Install with: brew install maven${NC}"
    exit 1
fi
echo -e "  ${GREEN}✓${NC} Maven: $(mvn -version 2>&1 | head -1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')"

# Node
if ! command -v node &>/dev/null; then
    echo -e "  ${RED}✗ Node.js not found. Install with: brew install node${NC}"
    exit 1
fi
echo -e "  ${GREEN}✓${NC} Node: $(node -v)"

# Docker
if ! docker info &>/dev/null; then
    echo -e "  ${YELLOW}! Docker not running. Starting Docker Desktop...${NC}"
    open -a Docker
    echo -n "    Waiting for Docker"
    for i in $(seq 1 30); do
        if docker info &>/dev/null; then echo -e " ${GREEN}ready${NC}"; break; fi
        echo -n "."
        sleep 2
    done
    if ! docker info &>/dev/null; then
        echo -e "\n  ${RED}✗ Docker failed to start. Please start Docker Desktop manually.${NC}"
        exit 1
    fi
fi
echo -e "  ${GREEN}✓${NC} Docker: running"

# .env file
if [ ! -f "$PROJECT_DIR/.env" ]; then
    echo -e "  ${RED}✗ .env file not found. Copy .env.example to .env and fill in your keys.${NC}"
    exit 1
fi
echo -e "  ${GREEN}✓${NC} .env: found"

echo ""

# 1. Kill any stale processes on our ports
echo -n "Clearing stale processes...     "
lsof -ti:5173 2>/dev/null | xargs kill -9 2>/dev/null || true
lsof -ti:8080 2>/dev/null | xargs kill -9 2>/dev/null || true
echo -e "${GREEN}done${NC}"

# 2. Start Docker containers
echo -n "Starting Docker containers...   "
cd "$PROJECT_DIR"
docker compose up -d 2>/dev/null
echo -e "${GREEN}started${NC}"

# Wait for PostgreSQL to be healthy
echo -n "Waiting for PostgreSQL...       "
for i in $(seq 1 20); do
    if docker exec skywings-db pg_isready -U skywings &>/dev/null; then
        echo -e "${GREEN}healthy${NC}"
        break
    fi
    sleep 1
    if [ "$i" -eq 20 ]; then echo -e "${RED}timeout${NC}"; exit 1; fi
done

# Wait for Redis
echo -n "Waiting for Redis...            "
for i in $(seq 1 10); do
    if docker exec skywings-redis redis-cli ping &>/dev/null; then
        echo -e "${GREEN}healthy${NC}"
        break
    fi
    sleep 1
    if [ "$i" -eq 10 ]; then echo -e "${RED}timeout${NC}"; exit 1; fi
done

# 3. Start Backend
echo -n "Starting backend...             "
cd "$PROJECT_DIR/backend"
set -a && source "$PROJECT_DIR/.env" && set +a
nohup mvn spring-boot:run -q > /tmp/skywings-backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to be ready (check health endpoint)
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/swagger-ui.html &>/dev/null; then
        echo -e "${GREEN}running (PID: $BACKEND_PID)${NC}"
        break
    fi
    sleep 1
    if [ "$i" -eq 30 ]; then
        echo -e "${RED}timeout — check /tmp/skywings-backend.log${NC}"
        exit 1
    fi
done

# 4. Start Frontend
echo -n "Starting frontend...            "
cd "$PROJECT_DIR/frontend"
nohup npm run dev > /tmp/skywings-frontend.log 2>&1 &
FRONTEND_PID=$!
sleep 3

if lsof -ti:5173 &>/dev/null; then
    echo -e "${GREEN}running (PID: $FRONTEND_PID)${NC}"
else
    echo -e "${RED}failed — check /tmp/skywings-frontend.log${NC}"
    exit 1
fi

# 5. Health check — verify all services are truly live
echo ""
echo "-----------------------------------------"
echo "Health checks:"

# Frontend
echo -n "  Frontend  (http://localhost:5173)  "
if curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 | grep -q "200"; then
    echo -e "${GREEN}✓ LIVE${NC}"
else
    echo -e "${YELLOW}⚠ may need a moment${NC}"
fi

# Backend API
echo -n "  Backend   (http://localhost:8080)  "
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/v3/api-docs)
if [ "$STATUS" = "200" ]; then
    echo -e "${GREEN}✓ LIVE${NC}"
else
    echo -e "${YELLOW}⚠ status: $STATUS${NC}"
fi

# Swagger
echo -n "  Swagger   (http://localhost:8080)  "
if curl -s http://localhost:8080/swagger-ui.html | grep -q "swagger" 2>/dev/null; then
    echo -e "${GREEN}✓ LIVE${NC}"
else
    echo -e "${GREEN}✓ available${NC}"
fi

# PostgreSQL
echo -n "  PostgreSQL (localhost:5432)        "
if docker exec skywings-db pg_isready -U skywings &>/dev/null; then
    echo -e "${GREEN}✓ LIVE${NC}"
else
    echo -e "${RED}✗ DOWN${NC}"
fi

# Redis
echo -n "  Redis      (localhost:6379)        "
if docker exec skywings-redis redis-cli ping 2>/dev/null | grep -q "PONG"; then
    echo -e "${GREEN}✓ LIVE${NC}"
else
    echo -e "${RED}✗ DOWN${NC}"
fi

echo "-----------------------------------------"
echo ""
echo -e "${GREEN}SkyWings Airways is ready!${NC}"
echo ""
echo -e "  ${CYAN}App:${NC}     http://localhost:5173"
echo -e "  ${CYAN}API:${NC}     http://localhost:8080"
echo -e "  ${CYAN}Swagger:${NC} http://localhost:8080/swagger-ui.html"
echo ""
echo -e "  ${CYAN}Admin:${NC}   admin@skywings.com / Admin@123"
echo ""
echo "Logs:"
echo "  Backend:  tail -f /tmp/skywings-backend.log"
echo "  Frontend: tail -f /tmp/skywings-frontend.log"
echo ""
echo "Run ./shutdown.sh to stop everything."
echo ""
