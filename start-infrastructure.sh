#!/bin/bash

echo "🚀 Starting Open-Spot Infrastructure Services..."

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

# Start Docker infrastructure
echo "📦 Starting Docker containers (PostgreSQL, Redis, Kafka)..."
docker-compose up -d

echo "⏳ Waiting for services to be ready..."
sleep 2

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
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo "  - Kafka: localhost:9092"
echo ""
echo "💡 To start domain services, run: ./start-services.sh"
echo "💡 To stop all services, run: ./stop-all.sh"