#!/bin/bash

echo "🚀 Starting Open-Spot Domain Services..."

# Check if infrastructure is running
if ! curl -f http://localhost:9999/actuator/health >/dev/null 2>&1; then
    echo "❌ Config Service is not running. Please start infrastructure first:"
    echo "   ./start-infrastructure.sh"
    exit 1
fi

echo "✅ Infrastructure is ready. Starting domain services..."

echo "🔐 Starting Auth Service..."
./gradlew :auth-service:bootRun --quiet > logs/auth-service.log 2>&1  &
AUTH_PID=$!

sleep 5

echo "📊 Starting Analysis Service..."
./gradlew :analysis-service:bootRun --quiet > logs/analysis-service.log 2>&1  &
ANALYSIS_PID=$!

sleep 5

echo "🔔 Starting Notification Service..."
./gradlew :notification-service:bootRun --quiet > logs/notification-service.log 2>&1  &
NOTIFICATION_PID=$!

echo "✅ All domain services started!"
echo ""
echo "📋 Service PIDs:"
echo "  - Auth Service: $AUTH_PID"
echo "  - Analysis Service: $ANALYSIS_PID"
echo "  - Notification Service: $NOTIFICATION_PID"
echo ""
echo "📋 Service URLs:"
echo "  - Auth Service: http://localhost:8081"
echo "  - Analysis Service: http://localhost:8082"  
echo "  - Notification Service: http://localhost:8083"
echo "  - Gateway (All APIs): http://localhost:8080"
echo ""
echo "💡 Test endpoints:"
echo "  - curl http://localhost:8080/api/v1/auth/health"
echo "  - curl http://localhost:8080/api/v1/stores/health"
echo ""
echo "💡 To stop all services, run: ./stop-all.sh"