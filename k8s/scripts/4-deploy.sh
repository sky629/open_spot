#!/bin/bash
set -e

echo "🚀 Deploying Open-Spot to Kubernetes..."

# Load .env file from project root
cd ../..
if [ ! -f ".env" ]; then
    echo "❌ .env file not found! Please copy .env.example to .env and configure it."
    exit 1
fi

# Source .env file
set -a
source .env
set +a

cd k8s/helm

RELEASE_NAME="openspot"
NAMESPACE="openspot"
VALUES_FILE="openspot/values.yaml"

# Extract secrets from .env
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-postgres}"
JWT_SECRET="${JWT_SECRET:-open-spot-jwt-secret-key-for-local-development-change-in-production}"
GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID:-your-google-client-id}"
GOOGLE_CLIENT_SECRET="${GOOGLE_CLIENT_SECRET:-your-google-client-secret}"

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
        helm upgrade ${RELEASE_NAME} openspot \
            --namespace ${NAMESPACE} \
            -f ${VALUES_FILE} \
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
    # Create namespace if it doesn't exist
    if ! kubectl get namespace ${NAMESPACE} &> /dev/null; then
        echo "Namespace ${NAMESPACE} not found. Creating it..."
        kubectl create namespace ${NAMESPACE}
    fi
    helm install ${RELEASE_NAME} openspot \
        --namespace ${NAMESPACE} \
        -f ${VALUES_FILE} \
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
    echo "   - https://api.openspot.kang-labs.com/api/v1/auth/health (공개)"
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
