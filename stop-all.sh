#!/bin/bash

echo "🛑 Stopping all Open-Spot services..."

# Stop Spring Boot applications
echo "🔴 Stopping Spring Boot services..."
pkill -f "gradle.*bootRun"

# Stop Docker containers
echo "🐳 Stopping Docker containers..."
docker-compose -f docker-compose.infra.yml down

echo "✅ All services stopped!"
echo ""
echo "💡 To start again:"
echo "  1. ./start-infrastructure.sh"
echo "  2. ./start-services.sh"