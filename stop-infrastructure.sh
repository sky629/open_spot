#!/bin/bash

echo "🛑 Stopping Open-Spot Infrastructure Services (Config + Gateway)..."

# Kill processes using service ports (more reliable)
PIDS=$(lsof -ti :9999,8080 2>/dev/null)
if [ -n "$PIDS" ]; then
    echo "🔴 Stopping Config and Gateway services..."
    kill $PIDS 2>/dev/null
    sleep 2
    # Force kill if still running
    PIDS=$(lsof -ti :9999,8080 2>/dev/null)
    if [ -n "$PIDS" ]; then
        echo "   Force stopping remaining processes..."
        kill -9 $PIDS 2>/dev/null
    fi
    echo "⏳ Waiting for services to stop..."
    sleep 1
    echo "✅ Infrastructure services stopped!"
else
    echo "ℹ️  No infrastructure services were running"
fi

# Also kill gradle bootRun processes as backup
pkill -f "gradle.*bootRun.*(config-service|gateway-service)" 2>/dev/null

echo ""
echo "💡 Docker containers (PostgreSQL, Redis, Kafka) are still running"
echo "💡 Domain services (Auth, Location, Notification) are still running"
echo ""
echo "💡 To start infrastructure, run: ./start-infrastructure.sh"
echo "💡 To stop everything, run: ./stop-all.sh"
