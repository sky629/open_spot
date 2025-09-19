#!/bin/bash

# Configuration Refresh Script - Refresh configuration for all services without restart
# Usage: ./refresh-config.sh [service-name]

set -e

# Load environment variables
if [ -f ".env" ]; then
    set -a
    source .env
    set +a
fi

SERVICE_NAME=${1:-"all"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_banner() {
    echo -e "${BLUE}=== Open-Spot Configuration Refresh ===${NC}"
    echo -e "${YELLOW}Target: $SERVICE_NAME${NC}"
    echo ""
}

# Function to refresh service configuration
refresh_service_config() {
    local service_name=$1
    local port=$2
    local display_name=$3
    
    echo -e "${BLUE}🔄 Refreshing configuration for $display_name...${NC}"
    
    # Check if service is running
    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        # Refresh configuration
        RESPONSE=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST "http://localhost:$port/actuator/refresh" 2>/dev/null || echo "HTTP_CODE:000")
        HTTP_CODE=$(echo "$RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
        
        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}✅ Configuration refreshed for $display_name${NC}"
        elif [ "$HTTP_CODE" = "000" ]; then
            echo -e "${RED}❌ Failed to connect to $display_name (port $port)${NC}"
        else
            echo -e "${YELLOW}⚠️  Refresh endpoint returned HTTP $HTTP_CODE for $display_name${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  $display_name is not running (port $port)${NC}"
    fi
}

# Function to refresh all services
refresh_all_services() {
    echo -e "${GREEN}🔄 Refreshing configuration for all services...${NC}"
    echo ""
    
    # Service configurations: name, port, display_name
    declare -a SERVICES=(
        "config-service 9999 Config-Service"
        "gateway-service 8080 Gateway-Service" 
        "auth-service 8081 Auth-Service"
        "location-service 8082 Location-Service"
        "notification-service 8083 Notification-Service"
    )
    
    for service_config in "${SERVICES[@]}"; do
        IFS=' ' read -r service_name port display_name <<< "$service_config"
        refresh_service_config "$service_name" "$port" "$display_name"
        echo ""
    done
}

# Function to check service health
check_service_health() {
    local port=$1
    local display_name=$2
    
    echo -e "${BLUE}🏥 Health check for $display_name...${NC}"
    
    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        HEALTH_RESPONSE=$(curl -s "http://localhost:$port/actuator/health" | grep -o '"status":"[^"]*' | cut -d'"' -f4)
        if [ "$HEALTH_RESPONSE" = "UP" ]; then
            echo -e "${GREEN}✅ $display_name is healthy${NC}"
        else
            echo -e "${YELLOW}⚠️  $display_name status: $HEALTH_RESPONSE${NC}"
        fi
    else
        echo -e "${RED}❌ $display_name is not responding${NC}"
    fi
}

# Function to show service info
show_service_info() {
    echo -e "${BLUE}📋 Service Information:${NC}"
    echo ""
    echo -e "${YELLOW}Config Server (port 9999)${NC}"
    echo "  Health: http://localhost:9999/actuator/health"
    echo "  Refresh: http://localhost:9999/actuator/refresh"
    echo ""
    echo -e "${YELLOW}Gateway Service (port 8080)${NC}" 
    echo "  Health: http://localhost:8080/actuator/health"
    echo "  Refresh: http://localhost:8080/actuator/refresh"
    echo "  Routes: http://localhost:8080/actuator/gateway/routes"
    echo ""
    echo -e "${YELLOW}Auth Service (port 8081)${NC}"
    echo "  Health: http://localhost:8081/actuator/health"
    echo "  Refresh: http://localhost:8081/actuator/refresh"
    echo ""
    echo -e "${YELLOW}Location Service (port 8082)${NC}"
    echo "  Health: http://localhost:8082/actuator/health"
    echo "  Refresh: http://localhost:8082/actuator/refresh"
    echo ""
    echo -e "${YELLOW}Notification Service (port 8083)${NC}"
    echo "  Health: http://localhost:8083/actuator/health"
    echo "  Refresh: http://localhost:8083/actuator/refresh"
}

# Main execution
print_banner

case $SERVICE_NAME in
    "all")
        refresh_all_services
        ;;
    "config-service" | "config")
        refresh_service_config "config-service" "9999" "Config-Service"
        ;;
    "gateway-service" | "gateway")
        refresh_service_config "gateway-service" "8080" "Gateway-Service"
        ;;
    "auth-service" | "auth")
        refresh_service_config "auth-service" "8081" "Auth-Service"
        ;;
    "location-service" | "location")
        refresh_service_config "location-service" "8082" "Location-Service"
        ;;
    "notification-service" | "notification")
        refresh_service_config "notification-service" "8083" "Notification-Service"
        ;;
    "health")
        echo -e "${GREEN}🏥 Running health checks for all services...${NC}"
        echo ""
        check_service_health "9999" "Config-Service"
        check_service_health "8080" "Gateway-Service"  
        check_service_health "8081" "Auth-Service"
        check_service_health "8082" "Location-Service"
        check_service_health "8083" "Notification-Service"
        ;;
    "info")
        show_service_info
        ;;
    *)
        echo -e "${RED}❌ Unknown service: $SERVICE_NAME${NC}"
        echo ""
        echo -e "${YELLOW}Available services:${NC}"
        echo "  all, config, gateway, auth, location, notification"
        echo ""
        echo -e "${YELLOW}Additional commands:${NC}"
        echo "  health - Check health of all services"
        echo "  info - Show service endpoint information"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}🎉 Configuration refresh completed!${NC}"