#!/bin/bash

# ==============================================================================
# Minikube Configuration for Open-Spot
# ==============================================================================
# macOS에서 Docker Desktop 드라이버를 사용한 minikube 클러스터 설정
#
# 사용법:
#   ./minikube-config.sh start   # 클러스터 생성 및 시작
#   ./minikube-config.sh stop    # 클러스터 정지
#   ./minikube-config.sh delete  # 클러스터 삭제
#   ./minikube-config.sh status  # 클러스터 상태 확인
# ==============================================================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 설정값
CLUSTER_NAME="openspot"
DRIVER="docker"           # macOS Docker Desktop 사용
CPUS="2"                  # CPU 코어 수 (권장: 4+)
MEMORY="4096"             # 메모리 MB (권장: 8GB+)
DISK_SIZE="20g"           # 디스크 크기 (권장: 40GB+)
K8S_VERSION="v1.31.0"     # Kubernetes 버전

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

# 함수: minikube 설치 확인
check_minikube() {
    if ! command -v minikube &> /dev/null; then
        log_error "minikube가 설치되어 있지 않습니다."
        log_info "Homebrew로 설치: brew install minikube"
        exit 1
    fi
    log_info "minikube 버전: $(minikube version --short)"
}

# 함수: Docker 확인
check_docker() {
    if ! docker info &> /dev/null; then
        log_error "Docker가 실행 중이지 않습니다."
        log_info "Docker Desktop을 시작해주세요."
        exit 1
    fi
    log_info "Docker가 실행 중입니다."
}

# 함수: 클러스터 시작
start_cluster() {
    log_info "Minikube 클러스터를 생성합니다..."
    log_info "설정: CPU=${CPUS}, Memory=${MEMORY}MB, Disk=${DISK_SIZE}, K8s=${K8S_VERSION}"

    # 기존 클러스터 확인
    if minikube profile list 2>/dev/null | grep -q "$CLUSTER_NAME"; then
        log_warn "클러스터 '$CLUSTER_NAME'가 이미 존재합니다."
        read -p "기존 클러스터를 삭제하고 새로 만드시겠습니까? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            minikube delete -p "$CLUSTER_NAME"
        else
            log_info "기존 클러스터를 시작합니다..."
            minikube start -p "$CLUSTER_NAME"
            return
        fi
    fi

    # 새 클러스터 생성
    minikube start \
        -p "$CLUSTER_NAME" \
        --driver="$DRIVER" \
        --cpus="$CPUS" \
        --memory="$MEMORY" \
        --disk-size="$DISK_SIZE" \
        --kubernetes-version="$K8S_VERSION" \
        --container-runtime=containerd \
        --addons=ingress \
        --addons=metrics-server \
        --addons=dashboard

    log_info "클러스터 생성 완료!"

    # kubectl context 설정
    kubectl config use-context "$CLUSTER_NAME"

    # openspot namespace 생성
    log_info "openspot namespace 생성..."
    kubectl create namespace openspot --dry-run=client -o yaml | kubectl apply -f -

    # /etc/hosts 업데이트
    update_hosts

    # 클러스터 정보 출력
    log_info "클러스터 정보:"
    minikube profile list
    kubectl cluster-info

    log_info ""
    log_info "========================================="
    log_info "Minikube 클러스터가 준비되었습니다!"
    log_info "========================================="
    log_info "Dashboard 실행: minikube dashboard -p $CLUSTER_NAME"
    log_info "Ingress IP: $(minikube ip -p $CLUSTER_NAME)"
    log_info "접속 URL: http://openspot.local"
    log_info "========================================="
}

# 함수: /etc/hosts 업데이트
update_hosts() {
    local MINIKUBE_IP=$(minikube ip -p "$CLUSTER_NAME")
    local HOSTS_ENTRY="$MINIKUBE_IP openspot.local"

    if grep -q "openspot.local" /etc/hosts; then
        log_warn "/etc/hosts에 openspot.local 엔트리가 이미 존재합니다."
        log_info "IP를 업데이트하려면 수동으로 수정하거나 아래 명령을 실행하세요:"
        log_info "sudo sed -i '' '/openspot.local/d' /etc/hosts"
        log_info "echo '$HOSTS_ENTRY' | sudo tee -a /etc/hosts"
    else
        log_info "/etc/hosts에 openspot.local 추가..."
        echo "$HOSTS_ENTRY" | sudo tee -a /etc/hosts > /dev/null
        log_info "추가 완료: $HOSTS_ENTRY"
    fi
}

# 함수: 클러스터 정지
stop_cluster() {
    log_info "클러스터를 정지합니다..."
    minikube stop -p "$CLUSTER_NAME"
    log_info "클러스터가 정지되었습니다."
}

# 함수: 클러스터 삭제
delete_cluster() {
    log_warn "클러스터를 삭제하면 모든 데이터가 손실됩니다!"
    read -p "정말로 삭제하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "클러스터를 삭제합니다..."
        minikube delete -p "$CLUSTER_NAME"
        log_info "클러스터가 삭제되었습니다."

        # /etc/hosts 정리
        log_info "/etc/hosts에서 openspot.local 제거..."
        sudo sed -i '' '/openspot.local/d' /etc/hosts || true
    else
        log_info "삭제가 취소되었습니다."
    fi
}

# 함수: 클러스터 상태 확인
status_cluster() {
    log_info "클러스터 상태:"
    minikube status -p "$CLUSTER_NAME"

    log_info ""
    log_info "노드 정보:"
    kubectl get nodes

    log_info ""
    log_info "리소스 사용량:"
    kubectl top nodes || log_warn "metrics-server가 아직 준비되지 않았습니다."
}

# 메인 로직
main() {
    check_minikube
    check_docker

    case "${1:-start}" in
        start)
            start_cluster
            ;;
        stop)
            stop_cluster
            ;;
        delete)
            delete_cluster
            ;;
        status)
            status_cluster
            ;;
        *)
            log_error "알 수 없는 명령: $1"
            echo "사용법: $0 {start|stop|delete|status}"
            exit 1
            ;;
    esac
}

main "$@"
