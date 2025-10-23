#!/bin/bash

# ==============================================================================
# Nginx Ingress Controller Installation via Helm
# ==============================================================================
# 모든 Kubernetes 환경 (minikube, EKS, GKE, AKS)에서 동일하게 작동
# 사용법:
#   ./install-ingress.sh
# ==============================================================================

set -e

CLUSTER_NAME="openspot"
NAMESPACE="ingress-nginx"
RELEASE_NAME="ingress-nginx"
CHART_REPO="ingress-nginx"
CHART_NAME="ingress-nginx/ingress-nginx"
CHART_VERSION="4.10.0"

# 스크립트 디렉토리
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"
HELM_VALUES_FILE="${K8S_DIR}/helm/ingress-nginx/values.yaml"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수: 로그 출력
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Helm 설치 확인
check_helm() {
    if ! command -v helm &> /dev/null; then
        log_error "Helm이 설치되어 있지 않습니다."
        log_info "Homebrew로 설치: brew install helm"
        exit 1
    fi
    log_info "Helm 버전: $(helm version --short)"
}

# Helm 저장소 추가
add_helm_repo() {
    log_info "Helm 저장소 추가: ${CHART_REPO}"

    if helm repo list | grep -q "^${CHART_REPO}"; then
        log_info "Helm 저장소 '${CHART_REPO}'가 이미 존재합니다. 업데이트 중..."
        helm repo update ${CHART_REPO}
    else
        helm repo add ${CHART_REPO} https://kubernetes.github.io/ingress-nginx
        helm repo update
    fi
}

# Namespace 생성
create_namespace() {
    log_info "Namespace '${NAMESPACE}' 생성..."
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
}

# Ingress Controller 설치
install_ingress() {
    log_info "Nginx Ingress Controller 설치..."
    log_info "  - Release: ${RELEASE_NAME}"
    log_info "  - Namespace: ${NAMESPACE}"
    log_info "  - Chart: ${CHART_NAME}:${CHART_VERSION}"
    log_info "  - Values: ${HELM_VALUES_FILE}"

    # Values 파일 존재 확인
    if [ ! -f "${HELM_VALUES_FILE}" ]; then
        log_error "Values 파일을 찾을 수 없습니다: ${HELM_VALUES_FILE}"
        exit 1
    fi

    # 기존 릴리스 확인
    if helm list -n ${NAMESPACE} | grep -q "^${RELEASE_NAME}"; then
        log_warn "릴리스 '${RELEASE_NAME}'이 이미 존재합니다."
        read -p "기존 릴리스를 제거하고 새로 설치하시겠습니까? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            log_info "기존 릴리스 제거..."
            helm uninstall ${RELEASE_NAME} -n ${NAMESPACE}
            sleep 5  # 안정화 대기
        else
            log_info "기존 릴리스 유지"
            return 0
        fi
    fi

    # Helm 설치
    helm install ${RELEASE_NAME} ${CHART_NAME} \
        --version ${CHART_VERSION} \
        --namespace ${NAMESPACE} \
        -f ${HELM_VALUES_FILE} \
        --timeout 5m

    log_info "Nginx Ingress Controller 설치 완료!"
}

# 설치 대기
wait_for_ingress() {
    log_info "Ingress Controller Pod 준비 대기 중..."
    kubectl wait --for=condition=Ready pods \
        -n ${NAMESPACE} \
        -l app.kubernetes.io/name=ingress-nginx \
        --timeout=300s || {
        log_warn "Pod 준비 타임아웃. 상태를 확인하세요."
        return 1
    }
    log_info "Ingress Controller가 준비되었습니다!"
}

# 서비스 상태 확인
check_service() {
    log_info ""
    log_info "========================================="
    log_info "Ingress Controller 서비스 상태:"
    log_info "========================================="

    # 서비스 정보
    kubectl get svc -n ${NAMESPACE} -l app.kubernetes.io/name=ingress-nginx

    log_info ""
    log_info "서비스 타입: LoadBalancer"
    log_info ""

    # LoadBalancer 외부 IP 확인
    EXTERNAL_IP=$(kubectl get svc -n ${NAMESPACE} -l app.kubernetes.io/name=ingress-nginx \
        -o jsonpath='{.items[0].status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "pending")

    if [ "${EXTERNAL_IP}" != "pending" ] && [ ! -z "${EXTERNAL_IP}" ]; then
        log_info "✅ 외부 IP: ${EXTERNAL_IP}"
    else
        log_warn "⏳ 외부 IP 할당 대기 중... (minikube tunnel 실행 필요)"
    fi

    log_info ""
    log_info "💡 다음 단계:"
    log_info "   1. 다른 터미널에서 minikube tunnel 실행:"
    log_info "      minikube tunnel -p ${CLUSTER_NAME}"
    log_info "   2. 접속 테스트:"
    log_info "      curl -H 'Host: openspot.local' http://localhost/api/v1/auth/health"
    log_info "========================================="
}

# 메인 로직
main() {
    log_info ""
    log_info "========================================="
    log_info "Nginx Ingress Controller 설치 시작"
    log_info "========================================="
    log_info ""

    check_helm
    add_helm_repo
    create_namespace
    install_ingress

    # Pod 준비 대기 (타임아웃 용인)
    wait_for_ingress || true

    check_service

    log_info ""
    log_info "✅ Ingress Controller 설치 완료!"
    log_info ""
}

main "$@"
