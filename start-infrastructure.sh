#!/bin/bash

echo "🚀 Starting Open-Spot Infrastructure Services..."

# Start Docker infrastructure
echo "📦 Starting Docker containers (PostgreSQL, Redis, Kafka)..."
docker-compose up -d

echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are running
echo "✅ Checking service health..."
docker-compose ps

echo "🔧 Starting Config Service..."
./gradlew :config-service:bootRun --quiet > logs/config-service.log 2>&1 &
CONFIG_PID=$!

echo "⏳ Waiting for Config Service to start..."
sleep 15

echo "🌐 Starting Gateway Service..."
./gradlew :gateway-service:bootRun --quiet > logs/gateway-service.log 2>&1 &
GATEWAY_PID=$!

echo "✅ Infrastructure services started!"
echo "Config Service PID: $CONFIG_PID"
echo "Gateway Service PID: $GATEWAY_PID"
echo ""
echo "📋 Service URLs:"
echo "  - Config Server: http://localhost:9999"
echo "  - Gateway: http://localhost:8080"
echo "  - PostgreSQL: localhost:15432"
echo "  - Redis: localhost:1ㄴ6379"
echo "  - Kafka: localhost:9092"
echo ""
echo "💡 To start domain services, run: ./start-services.sh"
echo "💡 To stop all services, run: ./stop-all.sh"