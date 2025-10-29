#!/bin/bash
set -e

# ========================================================================
# Script Path Detection
# 스크립트 위치를 기반으로 프로젝트 루트 자동 탐지
# ========================================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "🚀 Deploying Open-Spot to Kubernetes..."
echo "📂 Project Root: $PROJECT_ROOT"

# Load .env.k8s file from project root (Kubernetes 배포 전용)
cd "$PROJECT_ROOT"
if [ ! -f ".env.k8s" ]; then
    echo "❌ .env.k8s file not found! Please copy .env.example to .env.k8s and configure it."
    echo "   .env.k8s는 Kubernetes 배포 전용 환경 파일입니다."
    echo ""
    echo "   For local Docker development, use .env instead."
    echo "   로컬 Docker 개발에는 .env 파일을 사용하세요."
    exit 1
fi

# ========================================================================
# Load .env.k8s for Kubernetes deployment
# .env.k8s 파일 로드 (Kubernetes 배포 전용)
#
# This file contains:
# 1. Secrets (POSTGRES_PASSWORD, JWT_SECRET, Google OAuth)
# 2. Infrastructure settings with Kubernetes Service names
#    (REDIS_HOST=redis, POSTGRES_HOST=postgresql, etc.)
#
# ConfigMap and values.yaml will use these settings.
# ========================================================================
set -a
source .env.k8s
set +a

cd "$PROJECT_ROOT/k8s/helm"

RELEASE_NAME="openspot"
NAMESPACE="openspot"
VALUES_FILE="openspot/values.yaml"

# ========================================================================
# Load environment variables from .env.k8s
# .env.k8s에서 읽은 값을 Helm에 전달
# 기본값은 values.yaml에만 정의하므로 여기서 기본값 설정 불필요
# ========================================================================
# (변수들은 이미 source .env.k8s에서 로드됨)

# ========================================================================
# Validate required secrets from .env.k8s
# ========================================================================
if [ -z "${JWT_SECRET}" ] || [ -z "${POSTGRES_PASSWORD}" ]; then
    echo "⚠️  Warning: JWT_SECRET or POSTGRES_PASSWORD not set in .env.k8s"
    echo "   Using Helm values.yaml defaults (for development only)"
fi

# TLS 인증서 로드 (Cloudflare Origin)
PROJECT_ROOT="../.."
CERT_FILE="${PROJECT_ROOT}/origin_cert/kang-labs_origin_cert.pem"
KEY_FILE="${PROJECT_ROOT}/origin_cert/kang-labs_private.key"

if [ -f "$CERT_FILE" ] && [ -f "$KEY_FILE" ]; then
    echo "✅ TLS 인증서 파일 발견"
    TLS_CERT=$(cat "$CERT_FILE")
    TLS_KEY=$(cat "$KEY_FILE")
else
    echo "⚠️  TLS 인증서 파일을 찾을 수 없습니다."
    echo "   경로: $CERT_FILE, $KEY_FILE"
    echo "   HTTPS 사용 불가. HTTP로만 배포됩니다."
    TLS_CERT=""
    TLS_KEY=""
fi

# Check if Helm release exists
if helm list -n ${NAMESPACE} | grep -q "^${RELEASE_NAME}"; then
    echo "⚠️  Release '${RELEASE_NAME}' already exists"
    read -p "Do you want to upgrade it? (Y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Nn]$ ]]; then
        echo "⬆️  Upgrading Helm release..."
        echo "   (기본값은 values.yaml, 오버라이드는 .env.k8s 값)"
        helm upgrade ${RELEASE_NAME} openspot \
            --namespace ${NAMESPACE} \
            -f ${VALUES_FILE} \
            --set "config.frontendBaseUrl=${FRONTEND_BASE_URL}" \
            --set "config.oauth2RedirectUri=${OAUTH2_REDIRECT_URI}" \
            --set "config.frontendAuthSuccessPath=${FRONTEND_AUTH_SUCCESS_PATH}" \
            --set "config.frontendAuthErrorPath=${FRONTEND_AUTH_ERROR_PATH}" \
            --set "config.postgresHost=${POSTGRES_HOST}" \
            --set "config.postgresPort=${POSTGRES_PORT}" \
            --set "config.postgresDb=${POSTGRES_DB}" \
            --set "config.postgresUser=${POSTGRES_USER}" \
            --set "config.redisHost=${REDIS_HOST}" \
            --set "config.redisPort=${REDIS_PORT}" \
            --set "config.kafkaBootstrapServers=${KAFKA_BOOTSTRAP_SERVERS}" \
            --set "config.zookeeperConnect=${ZOOKEEPER_CONNECT}" \
            --set "config.authServiceHost=${AUTH_SERVICE_HOST}" \
            --set "config.authServicePort=${AUTH_SERVICE_PORT}" \
            --set "config.locationServiceHost=${LOCATION_SERVICE_HOST}" \
            --set "config.locationServicePort=${LOCATION_SERVICE_PORT}" \
            --set "config.notificationServiceHost=${NOTIFICATION_SERVICE_HOST}" \
            --set "config.notificationServicePort=${NOTIFICATION_SERVICE_PORT}" \
            --set "config.springCloudConfigUri=${SPRING_CLOUD_CONFIG_URI}" \
            --set "secrets.postgres.password=${POSTGRES_PASSWORD}" \
            --set "secrets.jwt.secret=${JWT_SECRET}" \
            --set "secrets.google.clientId=${GOOGLE_CLIENT_ID}" \
            --set "secrets.google.clientSecret=${GOOGLE_CLIENT_SECRET}" \
            --set "tls.cert=${TLS_CERT}" \
            --set "tls.key=${TLS_KEY}" \
            --wait \
            --timeout 15m
        echo "✅ Helm release upgraded"
    else
        echo "ℹ️  Skipping deployment"
        exit 0
    fi
else
    echo "📦 Installing Helm release..."
    echo "   (기본값은 values.yaml, 오버라이드는 .env.k8s 값)"
    # Create namespace if it doesn't exist
    if ! kubectl get namespace ${NAMESPACE} &> /dev/null; then
        echo "Namespace ${NAMESPACE} not found. Creating it..."
        kubectl create namespace ${NAMESPACE}
    fi
    helm install ${RELEASE_NAME} openspot \
        --namespace ${NAMESPACE} \
        -f ${VALUES_FILE} \
        --set "config.frontendBaseUrl=${FRONTEND_BASE_URL}" \
        --set "config.oauth2RedirectUri=${OAUTH2_REDIRECT_URI}" \
        --set "config.frontendAuthSuccessPath=${FRONTEND_AUTH_SUCCESS_PATH}" \
        --set "config.frontendAuthErrorPath=${FRONTEND_AUTH_ERROR_PATH}" \
        --set "config.postgresHost=${POSTGRES_HOST}" \
        --set "config.postgresPort=${POSTGRES_PORT}" \
        --set "config.postgresDb=${POSTGRES_DB}" \
        --set "config.postgresUser=${POSTGRES_USER}" \
        --set "config.redisHost=${REDIS_HOST}" \
        --set "config.redisPort=${REDIS_PORT}" \
        --set "config.kafkaBootstrapServers=${KAFKA_BOOTSTRAP_SERVERS}" \
        --set "config.zookeeperConnect=${ZOOKEEPER_CONNECT}" \
        --set "config.authServiceHost=${AUTH_SERVICE_HOST}" \
        --set "config.authServicePort=${AUTH_SERVICE_PORT}" \
        --set "config.locationServiceHost=${LOCATION_SERVICE_HOST}" \
        --set "config.locationServicePort=${LOCATION_SERVICE_PORT}" \
        --set "config.notificationServiceHost=${NOTIFICATION_SERVICE_HOST}" \
        --set "config.notificationServicePort=${NOTIFICATION_SERVICE_PORT}" \
        --set "config.springCloudConfigUri=${SPRING_CLOUD_CONFIG_URI}" \
        --set "secrets.postgres.password=${POSTGRES_PASSWORD}" \
        --set "secrets.jwt.secret=${JWT_SECRET}" \
        --set "secrets.google.clientId=${GOOGLE_CLIENT_ID}" \
        --set "secrets.google.clientSecret=${GOOGLE_CLIENT_SECRET}" \
        --set "tls.cert=${TLS_CERT}" \
        --set "tls.key=${TLS_KEY}" \
        --wait \
        --timeout 15m
    echo "✅ Helm release installed"
fi

echo ""
echo "⏳ Waiting for pods to be ready..."
echo "💡 This may take 2-5 minutes for PostgreSQL and services to initialize..."
echo ""

# Wait for infrastructure services first (longer timeout for databases)
echo "📊 Waiting for infrastructure services (PostgreSQL, Redis, Kafka, Zookeeper)..."
kubectl wait --for=condition=Ready pods -n ${NAMESPACE} \
    -l 'app in (postgresql,redis,kafka,zookeeper)' \
    --timeout=600s 2>/dev/null || {
    echo "⚠️  Infrastructure services not ready yet. Continuing..."
}

# Wait for application services
echo "📊 Waiting for application services..."
kubectl wait --for=condition=Ready pods -n ${NAMESPACE} \
    -l 'app in (config-service,gateway-service,auth-service,location-service,notification-service)' \
    --timeout=300s || {
    echo "⚠️  Some pods are not ready yet. Checking status..."
}

echo ""
echo "📊 Current pod status:"
kubectl get pods -n ${NAMESPACE}
echo ""
echo "💡 Useful debugging commands:"
echo "   - kubectl describe pod <pod-name> -n ${NAMESPACE}"
echo "   - kubectl logs <pod-name> -n ${NAMESPACE}"
echo "   - kubectl top pods -n ${NAMESPACE}"

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📋 Pod Status:"
kubectl get pods -n ${NAMESPACE}
echo ""
echo "📋 Services:"
kubectl get svc -n ${NAMESPACE}
echo ""
echo "📋 Ingress:"
kubectl get ingress -n ${NAMESPACE}
echo ""
echo "💡 Access the application:"
if [ ! -z "$TLS_CERT" ]; then
    echo "   - https://api.kang-labs.com/api/v1/auth/health (공개)"
    echo "   - https://openspot.local/api/v1/auth/health (로컬)"
else
    echo "   - http://openspot.local/api/v1/auth/health"
    echo "   - http://openspot.local/api/v1/locations/health"
fi
echo ""
echo "💡 Useful commands:"
echo "   - kubectl get pods -n ${NAMESPACE}"
echo "   - kubectl logs -f <pod-name> -n ${NAMESPACE}"
echo "   - helm list -n ${NAMESPACE}"
