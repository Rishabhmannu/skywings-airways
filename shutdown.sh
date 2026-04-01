#!/bin/bash
# SkyWings Airways — Graceful Shutdown Script
# Stops: Frontend (Vite) → Backend (Spring Boot) → Docker (PostgreSQL + Redis)

set -e
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo ""
echo "========================================="
echo "  SkyWings Airways — Shutting Down"
echo "========================================="
echo ""

# 1. Stop Frontend (Vite dev server on port 5173)
echo -n "Stopping frontend (port 5173)... "
FRONTEND_PIDS=$(lsof -ti:5173 2>/dev/null || true)
if [ -n "$FRONTEND_PIDS" ]; then
    echo "$FRONTEND_PIDS" | xargs kill 2>/dev/null || true
    sleep 1
    # Force kill if still alive
    REMAINING=$(lsof -ti:5173 2>/dev/null || true)
    if [ -n "$REMAINING" ]; then
        echo "$REMAINING" | xargs kill -9 2>/dev/null || true
    fi
    echo -e "${GREEN}stopped${NC}"
else
    echo -e "${YELLOW}not running${NC}"
fi

# 2. Stop Backend (Spring Boot on port 8080)
echo -n "Stopping backend (port 8080)...  "
BACKEND_PIDS=$(lsof -ti:8080 2>/dev/null || true)
if [ -n "$BACKEND_PIDS" ]; then
    echo "$BACKEND_PIDS" | xargs kill 2>/dev/null || true
    sleep 2
    # Force kill if still alive
    REMAINING=$(lsof -ti:8080 2>/dev/null || true)
    if [ -n "$REMAINING" ]; then
        echo "$REMAINING" | xargs kill -9 2>/dev/null || true
    fi
    echo -e "${GREEN}stopped${NC}"
else
    echo -e "${YELLOW}not running${NC}"
fi

# 3. Stop Docker containers
echo -n "Stopping Docker containers...   "
cd "$PROJECT_DIR"
if docker compose ps --status running 2>/dev/null | grep -q "skywings"; then
    docker compose stop 2>/dev/null
    echo -e "${GREEN}stopped${NC}"
else
    echo -e "${YELLOW}not running${NC}"
fi

# 4. Clean up any leftover Maven/Node processes for this project
echo -n "Cleaning up stale processes...  "
pkill -f "spring-boot:run.*skywings" 2>/dev/null || true
pkill -f "vite.*frontend" 2>/dev/null || true
echo -e "${GREEN}done${NC}"

# 5. Final verification
echo ""
echo "-----------------------------------------"
echo -n "Port 5173 (frontend): "
if lsof -ti:5173 &>/dev/null; then echo -e "${RED}STILL IN USE${NC}"; else echo -e "${GREEN}free${NC}"; fi
echo -n "Port 8080 (backend):  "
if lsof -ti:8080 &>/dev/null; then echo -e "${RED}STILL IN USE${NC}"; else echo -e "${GREEN}free${NC}"; fi
echo -n "Port 5432 (postgres): "
if docker compose ps --status running 2>/dev/null | grep -q "postgres"; then echo -e "${RED}STILL RUNNING${NC}"; else echo -e "${GREEN}stopped${NC}"; fi
echo -n "Port 6379 (redis):    "
if docker compose ps --status running 2>/dev/null | grep -q "redis"; then echo -e "${RED}STILL RUNNING${NC}"; else echo -e "${GREEN}stopped${NC}"; fi

echo "-----------------------------------------"
echo ""
echo -e "${GREEN}SkyWings Airways shut down complete.${NC}"
echo "Run ./start.sh to restart everything."
echo ""
