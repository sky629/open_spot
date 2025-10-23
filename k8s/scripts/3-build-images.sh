#!/bin/bash
set -e

echo "🐳 Building Docker Images for Minikube..."

CLUSTER_NAME="openspot"
PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

cd "$PROJECT_ROOT"

# 설정값
SERVICES=(
    "config-service"
    "gateway-service"
    "auth-service"
    "location-service"
    "notification-service"
)

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Minikube가 실행 중인지 확인
if ! minikube status -p "$CLUSTER_NAME" &>/dev/null; then
    echo -e "${RED}❌ Minikube cluster '$CLUSTER_NAME' is not running${NC}"
    echo "Please run: ./2-create-cluster.sh"
    exit 1
fi

echo -e "${GREEN}✅ Minikube cluster '$CLUSTER_NAME' is running${NC}"
echo ""

# 방법 1: minikube image load 사용 (권장)
# 이 방법이 더 안정적이고 명확함

echo -e "${YELLOW}📦 Building and loading images into Minikube...${NC}"
echo ""

for service in "${SERVICES[@]}"; do
    echo -e "${YELLOW}=========================================${NC}"
    echo -e "${YELLOW}Processing: ${service}${NC}"
    echo -e "${YELLOW}=========================================${NC}"

    # 서비스 모듈 디렉토리 찾기
    SERVICE_DIR=$(find . -maxdepth 2 -type d -name "*-${service}" 2>/dev/null | head -1)

    if [ -z "$SERVICE_DIR" ]; then
        echo -e "${RED}❌ Could not find directory for ${service}${NC}"
        continue
    fi

    echo -e "${YELLOW}📁 Found: ${SERVICE_DIR}${NC}"

    # 1. Docker 이미지 빌드
    echo -e "${YELLOW}🔨 Building Docker image: openspot/${service}:latest${NC}"
    docker build -t openspot/${service}:latest -f ${SERVICE_DIR}/Dockerfile .

    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Failed to build ${service}${NC}"
        continue
    fi

    echo -e "${GREEN}✅ Docker image built${NC}"

    # 2. 이미지를 Minikube로 로드
    echo -e "${YELLOW}📤 Loading image into Minikube cluster...${NC}"
    minikube image load openspot/${service}:latest -p "$CLUSTER_NAME"

    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Failed to load ${service} into Minikube${NC}"
        continue
    fi

    echo -e "${GREEN}✅ Image loaded into Minikube${NC}"
    echo ""
done

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ All images built and loaded!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""

# 로드된 이미지 확인
echo -e "${YELLOW}📋 Images loaded in Minikube cluster:${NC}"
minikube image ls -p "$CLUSTER_NAME" | grep openspot || echo "No openspot images found"

echo ""
echo -e "${GREEN}💡 Next step: Run ./4-deploy.sh to deploy services${NC}"
echo ""
echo -e "${YELLOW}💡 Useful commands:${NC}"
echo "   - Rebuild single image: docker build -t openspot/service:latest -f msa-modules/N-service/Dockerfile ."
echo "   - Load into minikube: minikube image load openspot/service:latest -p $CLUSTER_NAME"
echo "   - Remove all images: minikube image rm -p $CLUSTER_NAME \$(minikube image ls -p $CLUSTER_NAME | grep openspot)"
