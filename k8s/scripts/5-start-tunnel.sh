#!/bin/bash
set -e

# ========================================================================
# Script Path Detection
# 스크립트 위치를 기반으로 프로젝트 루트 자동 탐지
# ========================================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🚀 Starting minikube tunnel in background..."
echo "📂 Project Root: $PROJECT_ROOT"

CLUSTER_NAME="openspot"
PID_FILE="/tmp/minikube-tunnel-${CLUSTER_NAME}.pid"
LOG_FILE="/tmp/minikube-tunnel-${CLUSTER_NAME}.log"

# Check if tunnel is already running
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "⚠️  Tunnel is already running (PID: $OLD_PID)"
        echo "💡 To stop it, run: sh k8s/scripts/6-stop-tunnel.sh"
        exit 0
    else
        echo "🗑️  Cleaning up stale PID file..."
        rm -f "$PID_FILE"
    fi
fi

echo "sudo 권한 미리 갱신"
sudo -v

# Start tunnel in background with sudo (required for ports 80/443)
sudo nohup minikube tunnel -p "$CLUSTER_NAME" --bind-address=0.0.0.0 > "$LOG_FILE" 2>&1 &
NEW_PID=$!

# Save PID
echo "$NEW_PID" > "$PID_FILE"

echo "✅ Tunnel started successfully!"
echo ""
echo "📊 Details:"
echo "  Cluster: $CLUSTER_NAME"
echo "  PID: $NEW_PID"
echo "  Log file: $LOG_FILE"
echo ""
echo "🌐 Access via:"
echo "  http://localhost:8080/api/v1/auth/health"
echo ""
echo "💡 To view logs:"
echo "  tail -f $LOG_FILE"
echo ""
echo "💡 To stop tunnel:"
echo "  sh k8s/scripts/6-stop-tunnel.sh"
echo ""

# Wait a moment and check if process is still running
sleep 2
if kill -0 "$NEW_PID" 2>/dev/null; then
    echo "✓ Tunnel process is running"
else
    echo "❌ Tunnel process failed to start"
    echo "   Check log: tail -f $LOG_FILE"
    exit 1
fi
