#!/bin/bash

###############################################################################
# Smoke Tests for Open-Spot Microservices
#
# Usage: ./scripts/smoke-tests.sh [BASE_URL]
# Example: ./scripts/smoke-tests.sh http://localhost:8080
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URL (default: localhost)
BASE_URL="${1:-http://localhost:8080}"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Open-Spot Smoke Tests${NC}"
echo -e "${YELLOW}Base URL: $BASE_URL${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# Function to check service health
check_health() {
    local service_name=$1
    local health_url=$2
    local max_retries=30
    local retry_count=0

    echo -n "Checking $service_name health... "

    while [ $retry_count -lt $max_retries ]; do
        response=$(curl -s -o /dev/null -w "%{http_code}" "$health_url" || echo "000")

        if [ "$response" = "200" ]; then
            echo -e "${GREEN}✓ OK${NC}"
            return 0
        fi

        retry_count=$((retry_count + 1))
        sleep 2
    done

    echo -e "${RED}✗ FAILED (HTTP $response)${NC}"
    return 1
}

# Function to test API endpoint
test_endpoint() {
    local endpoint_name=$1
    local method=$2
    local url=$3
    local expected_status=$4

    echo -n "Testing $endpoint_name... "

    response=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" || echo "000")

    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✓ OK (HTTP $response)${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED (Expected: $expected_status, Got: $response)${NC}"
        return 1
    fi
}

# Health check tests
echo -e "${YELLOW}1. Health Checks${NC}"
echo "-------------------"

# Gateway를 통한 서비스 헬스 체크
check_health "Auth Service" "$BASE_URL/api/v1/auth/health" || exit 1
check_health "Location Service" "$BASE_URL/api/v1/locations/health" || exit 1
check_health "Notification Service" "$BASE_URL/api/v1/notifications/health" || exit 1

echo ""

# API endpoint tests
echo -e "${YELLOW}2. API Endpoint Tests${NC}"
echo "----------------------"

# Public endpoints (no auth required)
test_endpoint "Categories List" "GET" "$BASE_URL/api/v1/categories" "200" || exit 1

echo ""

# Service connectivity tests
echo -e "${YELLOW}3. Service Connectivity${NC}"
echo "-----------------------"

# Test database connectivity (through health checks)
echo -n "Database connectivity... "
auth_health=$(curl -s "$BASE_URL/api/v1/auth/health" | grep -o '"status":"UP"' || echo "")
if [ -n "$auth_health" ]; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    exit 1
fi

# Test Redis connectivity
echo -n "Redis connectivity... "
location_health=$(curl -s "$BASE_URL/api/v1/locations/health" | grep -o '"status":"UP"' || echo "")
if [ -n "$location_health" ]; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    exit 1
fi

echo ""

# Summary
echo -e "${YELLOW}========================================${NC}"
echo -e "${GREEN}All smoke tests passed! ✓${NC}"
echo -e "${YELLOW}========================================${NC}"

exit 0
