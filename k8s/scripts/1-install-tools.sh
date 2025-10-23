#!/bin/bash
set -e

echo "🔧 Installing Kubernetes Development Tools for Minikube..."

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo -e "${RED}❌ This script is designed for macOS.${NC}"
    echo "For other OS, please install tools manually:"
    echo "  - Linux: apt-get, pacman, or yum"
    echo "  - Windows: chocolatey or manual install"
    exit 1
fi

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo -e "${RED}❌ Homebrew is not installed.${NC}"
    echo "Please install it first:"
    echo '   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"'
    exit 1
fi

echo -e "${GREEN}✅ Homebrew is installed${NC}"

# Install kubectl
if ! command -v kubectl &> /dev/null; then
    echo -e "${YELLOW}📦 Installing kubectl...${NC}"
    brew install kubectl
    echo -e "${GREEN}✅ kubectl installed${NC}"
else
    KUBECTL_VERSION=$(kubectl version --client --short 2>/dev/null | cut -d' ' -f3)
    echo -e "${GREEN}✅ kubectl is already installed (${KUBECTL_VERSION})${NC}"
fi

# Install Helm
if ! command -v helm &> /dev/null; then
    echo -e "${YELLOW}📦 Installing Helm...${NC}"
    brew install helm
    echo -e "${GREEN}✅ Helm installed${NC}"
else
    HELM_VERSION=$(helm version --short | cut -d':' -f2)
    echo -e "${GREEN}✅ Helm is already installed (${HELM_VERSION})${NC}"
fi

# Install Minikube (required for Minikube deployment)
if ! command -v minikube &> /dev/null; then
    echo -e "${YELLOW}📦 Installing Minikube...${NC}"
    brew install minikube
    echo -e "${GREEN}✅ Minikube installed${NC}"
else
    MINIKUBE_VERSION=$(minikube version --short)
    echo -e "${GREEN}✅ Minikube is already installed (${MINIKUBE_VERSION})${NC}"
fi


# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed.${NC}"
    echo "Please install Docker Desktop:"
    echo "   https://www.docker.com/products/docker-desktop"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker daemon is not running.${NC}"
    echo "Please start Docker Desktop first."
    exit 1
fi

DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | tr -d ',')
echo -e "${GREEN}✅ Docker is running (${DOCKER_VERSION})${NC}"

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ All tools are installed and ready!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "📋 Installed versions:"
KUBECTL_VERSION=$(kubectl version --client --short 2>/dev/null | cut -d' ' -f3)
echo "   - kubectl: ${KUBECTL_VERSION}"
HELM_VERSION=$(helm version --short | cut -d':' -f2)
echo "   - Helm: ${HELM_VERSION}"
MINIKUBE_VERSION=$(minikube version --short)
echo "   - Minikube: ${MINIKUBE_VERSION}"
DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | tr -d ',')
echo "   - Docker: ${DOCKER_VERSION}"

echo ""
echo -e "${GREEN}💡 Next steps:${NC}"
echo "   1. Run: cd $(dirname $0)"
echo "   2. Create cluster: ./2-create-cluster.sh"
echo "   3. Build images: ./3-build-images.sh"
echo "   4. Deploy: ./4-deploy.sh"
echo ""
echo -e "${YELLOW}💡 Useful commands:${NC}"
echo "   - minikube status -p openspot"
echo "   - minikube dashboard -p openspot"
echo "   - minikube ip -p openspot"
