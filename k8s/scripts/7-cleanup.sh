#!/bin/bash
set -e

# ========================================================================
# Script Path Detection
# 스크립트 위치를 기반으로 프로젝트 루트 자동 탐지
# ========================================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🧹 Cleaning up Open-Spot Minikube environment..."
echo "📂 Project Root: $PROJECT_ROOT"

CLUSTER_NAME="openspot"
NAMESPACE="openspot"
RELEASE_NAME="openspot"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 확인 요청
echo -e "${YELLOW}⚠️  This will delete the entire Kubernetes cluster and all data!${NC}"
read -p "Are you sure you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${GREEN}ℹ️  Cleanup cancelled${NC}"
    exit 0
fi

echo ""
echo -e "${YELLOW}Starting cleanup...${NC}"
echo ""

# 0. Ingress port-forward 중지
if [ -f /tmp/openspot/ingress.pid ]; then
    echo -e "${YELLOW}🛑 Stopping Ingress port-forward...${NC}"
    pkill -f "kubectl port-forward.*ingress-nginx-controller.*8080" || true
    rm -f /tmp/openspot/ingress.pid
    echo -e "${GREEN}✅ Ingress port-forward stopped${NC}"
    echo ""
fi

# 1. Helm release 제거
if helm list -n ${NAMESPACE} 2>/dev/null | grep -q "^${RELEASE_NAME}"; then
    echo -e "${YELLOW}🗑️  Uninstalling Helm release '${RELEASE_NAME}'...${NC}"
    helm uninstall ${RELEASE_NAME} -n ${NAMESPACE} || true
    echo -e "${GREEN}✅ Helm release uninstalled${NC}"
else
    echo -e "${YELLOW}ℹ️  Helm release not found${NC}"
fi

echo ""

# 2. Namespace 삭제
if kubectl get namespace ${NAMESPACE} &> /dev/null; then
    echo -e "${YELLOW}🗑️  Deleting namespace '${NAMESPACE}'...${NC}"
    kubectl delete namespace ${NAMESPACE} --timeout=60s || true
    echo -e "${GREEN}✅ Namespace deleted${NC}"
else
    echo -e "${YELLOW}ℹ️  Namespace not found${NC}"
fi

echo ""

# 3. Minikube 클러스터 정지 및 삭제
if minikube status -p ${CLUSTER_NAME} &>/dev/null; then
    echo -e "${YELLOW}🗑️  Stopping Minikube cluster '${CLUSTER_NAME}'...${NC}"
    minikube stop -p ${CLUSTER_NAME} || true
    echo -e "${GREEN}✅ Cluster stopped${NC}"

    echo ""
    echo -e "${YELLOW}🗑️  Deleting Minikube cluster '${CLUSTER_NAME}'...${NC}"
    minikube delete -p ${CLUSTER_NAME}
    echo -e "${GREEN}✅ Cluster deleted${NC}"
else
    echo -e "${YELLOW}ℹ️  Minikube cluster '${CLUSTER_NAME}' not found${NC}"
fi

echo ""

# 4. /etc/hosts 정리
echo -e "${YELLOW}🗑️  Removing openspot.local from /etc/hosts...${NC}"
if grep -q "openspot.local" /etc/hosts; then
    sudo sed -i '' '/openspot.local/d' /etc/hosts
    echo -e "${GREEN}✅ Removed openspot.local from /etc/hosts${NC}"
else
    echo -e "${YELLOW}ℹ️  openspot.local not found in /etc/hosts${NC}"
fi

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ Cleanup complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${YELLOW}💡 To start over:${NC}"
echo "   1. ./2-create-cluster.sh"
echo "   2. ./3-build-images.sh"
echo "   3. ./4-deploy.sh"
echo "   4. ./5-start-ingress.sh"
echo ""
echo -e "${YELLOW}💡 To only delete Helm release (keep cluster):${NC}"
echo "   helm uninstall ${RELEASE_NAME} -n ${NAMESPACE}"
echo ""
echo -e "${YELLOW}💡 To reset Minikube without deleting (restart):${NC}"
echo "   minikube stop -p ${CLUSTER_NAME}"
echo "   minikube start -p ${CLUSTER_NAME}"
echo ""
