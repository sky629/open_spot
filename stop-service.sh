#!/bin/bash

echo "🛑 Stopping Open-Spot services..."

# Stop Spring Boot applications
echo "🔴 Stopping Spring Boot services..."
pkill -f auth_service
pkill -f ananysis-service
pkill -f notification_service

echo "✅ All services stopped!"
echo ""
echo "💡 To start again:"
echo "  2. ./start-services.sh"