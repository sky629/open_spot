#!/bin/bash

echo "🛑 Stopping Open-Spot Domain Services..."

# Kill processes using service ports (more reliable)
PIDS=$(lsof -ti :8081,8082,8083 2>/dev/null)
if [ -n "$PIDS" ]; then
    echo "🔴 Stopping domain services (Auth, Location, Notification)..."
    kill $PIDS 2>/dev/null
    sleep 2
    # Force kill if still running
    PIDS=$(lsof -ti :8081,8082,8083 2>/dev/null)
    if [ -n "$PIDS" ]; then
        echo "   Force stopping remaining processes..."
        kill -9 $PIDS 2>/dev/null
    fi
    echo "⏳ Waiting for services to stop..."
    sleep 1
    echo "✅ Domain services stopped!"
else
    echo "ℹ️  No domain services were running"
fi

# Also kill gradle bootRun processes as backup
pkill -f "gradle.*bootRun.*(auth-service|location-service|notification-service)" 2>/dev/null

echo ""
echo "💡 Infrastructure services (Docker, Config, Gateway) are still running"
echo ""
echo "💡 To start domain services, run: ./start-services.sh"
echo "💡 To stop everything, run: ./stop-all.sh"
