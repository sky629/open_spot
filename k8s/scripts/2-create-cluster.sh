#!/bin/bash
set -e

echo "🚀 Creating Minikube Kubernetes Cluster..."

CLUSTER_NAME="openspot"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"

# Check if minikube is installed
if ! command -v minikube &> /dev/null; then
    echo "❌ minikube is not installed"
    echo "Install with: brew install minikube"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Docker is not running"
    echo "Please start Docker Desktop first"
    exit 1
fi

# Check if cluster already exists
if minikube profile list 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
    echo "⚠️  Cluster '${CLUSTER_NAME}' already exists"
    read -p "Do you want to delete and recreate it? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🗑️  Deleting existing cluster..."
        minikube delete -p ${CLUSTER_NAME}
    else
        echo "ℹ️  Using existing cluster"
        minikube start -p ${CLUSTER_NAME}
        exit 0
    fi
fi

# Make minikube-config.sh executable
chmod +x "${K8S_DIR}/minikube-config.sh"

# Create cluster using minikube-config.sh
echo "📦 Running minikube configuration..."
"${K8S_DIR}/minikube-config.sh" start

# Set kubectl context
echo "🔄 Setting kubectl context..."
kubectl config use-context ${CLUSTER_NAME}

# Wait for nodes to be ready
echo "⏳ Waiting for nodes to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=300s

# Get minikube IP
MINIKUBE_IP=$(minikube ip -p ${CLUSTER_NAME})

echo ""
echo "✅ Minikube cluster is ready!"
echo ""
echo "📋 Cluster info:"
echo "  Name: ${CLUSTER_NAME}"
echo "  IP: ${MINIKUBE_IP}"
echo "  URL: http://openspot.local"
echo ""
echo "📊 Cluster status:"
kubectl get nodes
echo ""
echo "💡 Next steps:"
echo "  1. Build images: cd $(dirname $SCRIPT_DIR)/scripts && ./3-build-images.sh"
echo "  2. Deploy services: ./4-deploy.sh"
echo "  3. View dashboard: minikube dashboard -p ${CLUSTER_NAME}"
