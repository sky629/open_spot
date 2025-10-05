#!/bin/bash

echo "🚀 Starting Open-Spot Domain Services..."

# Load environment variables from .env file
if [ -f .env ]; then
    echo "📄 Loading environment variables from .env file..."
    set -a
    source .env
    set +a
    echo "✅ Environment variables loaded"
else
    echo "⚠️ No .env file found. Using system environment variables."
fi

# Stop existing services if running
echo "🔍 Checking for existing services..."
EXISTING_SERVICES=$(pgrep -f "gradle.*bootRun.*(auth-service|location-service|notification-service)")
if [ -n "$EXISTING_SERVICES" ]; then
    echo "🛑 Stopping existing domain services..."
    pkill -f "gradle.*bootRun.*(auth-service|location-service|notification-service)"
    echo "⏳ Waiting for services to stop..."
    sleep 3
    echo "✅ Existing services stopped"
else
    echo "✅ No existing services running"
fi

# Health check function
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30

    echo "⏳ Waiting for $service_name to be healthy..."
    for i in $(seq 1 $max_attempts); do
        if curl -f -s "$url" > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        echo "   Attempt $i/$max_attempts..."
        sleep 2
    done
    echo "❌ $service_name failed to start!"
    return 1
}

# Check if infrastructure is running
if ! curl -f -s http://localhost:9999/actuator/health >/dev/null 2>&1; then
    echo "❌ Config Service is not running. Please start infrastructure first:"
    echo "   ./start-infrastructure.sh"
    exit 1
fi

if ! curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "❌ Gateway Service is not running. Please start infrastructure first:"
    echo "   ./start-infrastructure.sh"
    exit 1
fi

echo "✅ Infrastructure is ready. Starting domain services..."

echo "🔐 Starting Auth Service..."
./gradlew :auth-service:bootRun --quiet > logs/auth-service.log 2>&1 &
AUTH_PID=$!
echo "   Auth Service PID: $AUTH_PID"

echo "📊 Starting Location Service..."
./gradlew :location-service:bootRun --quiet > logs/location-service.log 2>&1 &
LOCATION_PID=$!
echo "   Location Service PID: $LOCATION_PID"

echo "🔔 Starting Notification Service..."
./gradlew :notification-service:bootRun --quiet > logs/notification-service.log 2>&1 &
NOTIFICATION_PID=$!
echo "   Notification Service PID: $NOTIFICATION_PID"

# Wait for all services to be healthy (in parallel)
echo ""
echo "⏳ Waiting for all services to start (this may take a minute)..."
wait_for_service "Auth Service" "http://localhost:8081/actuator/health" &
AUTH_WAIT_PID=$!
wait_for_service "Location Service" "http://localhost:8082/actuator/health" &
LOCATION_WAIT_PID=$!
wait_for_service "Notification Service" "http://localhost:8083/actuator/health" &
NOTIFICATION_WAIT_PID=$!

# Wait for all health checks to complete
wait $AUTH_WAIT_PID $LOCATION_WAIT_PID $NOTIFICATION_WAIT_PID

echo ""
echo "✅ All domain services started!"
echo ""
echo "📋 Service URLs:"
echo "  - Auth Service: http://localhost:8081"
echo "  - Location Service: http://localhost:8082"
echo "  - Notification Service: http://localhost:8083"
echo "  - Gateway (All APIs): http://localhost:8080"
echo ""
echo "💡 Test endpoints:"
echo "  - curl http://localhost:8080/api/v1/auth/health"
echo "  - curl http://localhost:8080/api/v1/locations/health"
echo ""
echo "💡 To stop all services, run: ./stop-all.sh"