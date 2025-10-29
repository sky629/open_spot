#!/bin/bash
set -e

# ========================================================================
# Script Path Detection
# 스크립트 위치를 기반으로 프로젝트 루트 자동 탐지
# ========================================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🛑 Stopping minikube tunnel..."
echo "📂 Project Root: $PROJECT_ROOT"

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

# Kill the process (use sudo if needed for background sudo processes)
echo "   Killing process (PID: $PID)..."
sudo kill "$PID" 2>/dev/null || kill "$PID" 2>/dev/null || true

# Wait for process to exit
TIMEOUT=5
COUNT=0
while kill -0 "$PID" 2>/dev/null && [ $COUNT -lt $TIMEOUT ]; do
    sleep 1
    COUNT=$((COUNT + 1))
done

# Force kill if still running (use sudo for background sudo processes)
if kill -0 "$PID" 2>/dev/null; then
    echo "   Force killing process..."
    sudo kill -9 "$PID" 2>/dev/null || kill -9 "$PID" 2>/dev/null || true
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
