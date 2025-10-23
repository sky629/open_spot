#!/bin/bash
set -e

echo "🛑 Stopping minikube tunnel..."

CLUSTER_NAME="openspot"
PID_FILE="/tmp/minikube-tunnel-${CLUSTER_NAME}.pid"
LOG_FILE="/tmp/minikube-tunnel-${CLUSTER_NAME}.log"

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    echo "⚠️  Tunnel PID file not found"
    echo "   The tunnel might not be running"
    exit 0
fi

# Read PID
PID=$(cat "$PID_FILE")

# Check if process is running
if ! kill -0 "$PID" 2>/dev/null; then
    echo "⚠️  Process (PID: $PID) is not running"
    echo "🗑️  Cleaning up PID file..."
    rm -f "$PID_FILE"
    exit 0
fi

# Kill the process
echo "   Killing process (PID: $PID)..."
kill "$PID" 2>/dev/null || true

# Wait for process to exit
TIMEOUT=5
COUNT=0
while kill -0 "$PID" 2>/dev/null && [ $COUNT -lt $TIMEOUT ]; do
    sleep 1
    COUNT=$((COUNT + 1))
done

# Force kill if still running
if kill -0 "$PID" 2>/dev/null; then
    echo "   Force killing process..."
    kill -9 "$PID" 2>/dev/null || true
    sleep 1
fi

# Clean up PID file
rm -f "$PID_FILE"

echo "✅ Tunnel stopped successfully!"
echo ""
echo "📊 Details:"
echo "  Cluster: $CLUSTER_NAME"
echo "  PID: $PID"
echo ""
echo "💡 To view the tunnel log (before deletion):"
echo "  tail -f $LOG_FILE"
echo ""
