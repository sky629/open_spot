# Open-Spot Kubernetes Deployment

Docker Desktop + Minikube를 이용한 Open-Spot MSA 로컬 Kubernetes 배포 환경

> **개발 환경 사양**
> - **Minikube**: 로컬 Kubernetes 클러스터
> - **드라이버**: Docker Desktop 기반
> - **리소스**: CPU 4코어, 메모리 8GB, 디스크 40GB
> - **Ingress**: NGINX Ingress Controller (자동 설치)

## 📋 목차

- [개요](#개요)
- [전제 조건](#전제-조건)
- [빠른 시작](#빠른-시작)
- [디렉토리 구조](#디렉토리-구조)
- [상세 사용법](#상세-사용법)
- [트러블슈팅](#트러블슈팅)
- [다음 단계](#다음-단계)

## 개요

이 디렉토리에는 Open-Spot MSA 백엔드를 Kubernetes 환경에 배포하기 위한 모든 설정 파일과 스크립트가 포함되어 있습니다.

### 주요 특징

- **Minikube (Kubernetes in Container)**: 로컬 Kubernetes 클러스터
- **Docker Desktop 드라이버**: macOS 최적화된 드라이버
- **Helm Charts**: 패키지 관리 및 배포
- **NGINX Ingress**: 자동 설치 (addon)
- **StatefulSets**: PostgreSQL, Redis, Kafka 상태 관리
- **Kubernetes Dashboard**: 내장 대시보드 UI
- **자동화 스크립트**: 원클릭 배포

## 전제 조건

### 필수 소프트웨어

- **macOS** (또는 Linux/Windows with WSL2)
- **Docker Desktop** 4.x 이상
- **Homebrew** (macOS)

### 필수 도구 설치

```bash
# Homebrew로 설치
brew install minikube kubectl helm

# 설치 확인
minikube version
kubectl version --client
helm version
```

**참고**: 자동 설치 스크립트 `1-install-tools.sh`도 제공됩니다.

## 빠른 시작

### 1. 도구 설치 (선택사항)

Minikube, kubectl, helm이 이미 설치되어 있으면 건너뛰세요.

```bash
cd k8s/scripts
./1-install-tools.sh
```

### 2. Kubernetes 클러스터 생성

```bash
./2-create-cluster.sh
```

이 스크립트는 자동으로:
- ✅ Minikube 클러스터 생성 (CPU 4코어, 메모리 8GB)
- ✅ NGINX Ingress Controller 설치 (addon)
- ✅ Kubernetes Metrics Server 설치 (addon)
- ✅ Kubernetes Dashboard 설치 (addon)
- ✅ `openspot` namespace 생성
- ✅ `/etc/hosts`에 `openspot.local` 추가

### 3. Docker 이미지 빌드 및 로드

```bash
./3-build-images.sh
```

### 4. Helm으로 배포

```bash
./4-deploy.sh
```

배포 중 진행 상황:
- 📊 인프라 서비스 대기 (PostgreSQL, Redis, Kafka, Zookeeper) - ~2분
- 📊 애플리케이션 서비스 대기 (Config, Gateway, Auth, Location, Notification) - ~1분
- ✅ 총 소요 시간: 약 3-5분

### 5. 외부 접근 설정 및 테스트

#### 로컬 접근
```bash
# Gateway 서비스 확인 (LoadBalancer EXTERNAL-IP)
kubectl get svc gateway-service -n openspot
# NAME              TYPE           EXTERNAL-IP   PORT(S)
# gateway-service   LoadBalancer   127.0.0.1     8080:xxxxx/TCP

# 로컬에서 접근
curl http://localhost:8080/api/v1/auth/health
curl http://localhost:8080/api/v1/locations/health
```

#### Cloudflare 도메인 연동 (프로덕션)

**1단계: Cloudflare DNS 설정**
```
- Type: A
- Name: api.openspot
- Content: <your-public-ip>  (예: 203.0.113.42)
- Proxy: OFF (회색 구름)
- TTL: Auto
```

**2단계: 공유기 포트포워딩 설정**
```
- 프로토콜: TCP
- 외부 포트: 443 (HTTPS)
- 내부 호스트: 컴퓨터 IP (예: 192.168.1.100)
- 내부 포트: 8080
```

**3단계: 테스트**
```bash
# 외부에서 접근 (HTTPS)
curl https://api.openspot.kang-labs.com/api/v1/auth/health
```

**4단계: 프론트엔드 연결**
```javascript
// 프론트엔드에서
const API_BASE_URL = 'https://api.openspot.kang-labs.com';
fetch(`${API_BASE_URL}/api/v1/auth/login`, {...})
```

#### 상태 확인
```bash
# Pod 상태 확인
kubectl get pods -n openspot

# Gateway LoadBalancer 확인
kubectl get svc gateway-service -n openspot

# Ingress 확인 (선택사항)
kubectl get ingress -n openspot

# Kubernetes Dashboard
minikube dashboard -p openspot
```

### 6. 클러스터 정리 및 삭제

```bash
./7-cleanup.sh
```

## 리소스 할당

### 기본 리소스 설정

Minikube에 할당된 리소스:

```bash
CPU: 4 cores
Memory: 8GB (8192 MB)
Disk: 40GB
```

### 리소스 변경 (선택사항)

리소스를 변경하려면 클러스터를 삭제하고 재생성하세요:

```bash
# 현재 리소스 확인
minikube config get cpus -p openspot
minikube config get memory -p openspot

# 클러스터 삭제
minikube delete -p openspot

# 리소스 변경 후 재생성
./2-create-cluster.sh
```

또는 `k8s/minikube-config.sh` 스크립트를 수정하여 CPUS, MEMORY 값을 변경할 수 있습니다.

## 디렉토리 구조

```
k8s/
├── minikube-config.sh                  # Minikube 클러스터 설정 스크립트
├── README.md                           # 이 파일
├── scripts/                            # 배포 자동화 스크립트
│   ├── 1-install-tools.sh             # 도구 설치
│   ├── 2-create-cluster.sh            # Minikube 클러스터 생성
│   ├── 3-build-images.sh              # 이미지 빌드
│   ├── 4-deploy.sh                    # Helm 배포
│   └── 5-cleanup.sh                   # 정리
└── helm/                               # Helm Charts
    └── openspot/
        ├── Chart.yaml                  # Chart 메타데이터
        ├── values.yaml                 # 기본 설정값 (Minikube 최적화)
        ├── values-dev.yaml             # 개발 환경 설정 (선택사항)
        └── templates/                  # Kubernetes 리소스 템플릿
            ├── _helpers.tpl            # 템플릿 헬퍼 함수
            ├── namespace.yaml          # Namespace
            ├── configmap.yaml          # ConfigMap
            ├── secrets.yaml            # Secrets
            ├── ingress.yaml            # Ingress
            ├── infrastructure/         # 인프라 서비스
            │   ├── postgresql-statefulset.yaml   # PostgreSQL + PostGIS
            │   ├── redis-statefulset.yaml        # Redis
            │   ├── kafka-statefulset.yaml        # Kafka
            │   └── zookeeper-statefulset.yaml    # Zookeeper
            └── microservices/          # 마이크로서비스
                ├── config-service.yaml
                ├── gateway-service.yaml
                ├── auth-service.yaml
                ├── location-service.yaml
                └── notification-service.yaml
```

## Minikube 명령어

### 클러스터 관리

```bash
# 클러스터 상태 확인
minikube status -p openspot

# 클러스터 일시 중지
minikube pause -p openspot

# 클러스터 재개
minikube unpause -p openspot

# 클러스터 정지
minikube stop -p openspot

# 클러스터 시작
minikube start -p openspot

# 클러스터 삭제
minikube delete -p openspot

# Minikube IP 확인
minikube ip -p openspot

# SSH로 노드 접속
minikube ssh -p openspot

# Kubernetes Dashboard 열기
minikube dashboard -p openspot

# 포트 포워딩 자동 설정
minikube tunnel -p openspot  # 로드밸런서 접근용
```

### 리소스 모니터링

```bash
# 노드 리소스 사용량
kubectl top nodes

# Pod 리소스 사용량
kubectl top pods -n openspot

# 리소스 부족 확인
kubectl describe node -p openspot
```

## 상세 사용법

### Helm Chart 수정

설정을 수정하려면 `helm/openspot/values.yaml`을 편집하세요:

```yaml
# 예: replicas 변경
configService:
  replicas: 2  # 1 -> 2로 변경

# 예: 리소스 제한 변경
authService:
  resources:
    limits:
      memory: "1Gi"  # 512Mi -> 1Gi로 변경

# 예: PostgreSQL 헬스체크 타임아웃 변경 (Minikube 최적화)
postgresql:
  healthCheck:
    liveness:
      initialDelaySeconds: 120  # 더 느린 환경용
    readiness:
      initialDelaySeconds: 90
```

### 개별 서비스 배포

```bash
# 전체 재배포
helm upgrade openspot ./helm/openspot -f ./helm/openspot/values-dev.yaml -n openspot

# 특정 서비스만 재시작
kubectl rollout restart deployment/auth-service -n openspot
kubectl rollout restart deployment/location-service -n openspot
```

### 로그 확인

```bash
# 특정 Pod 로그 확인
kubectl logs -f <pod-name> -n openspot

# 모든 Pod 로그 확인
kubectl logs -f -l app=auth-service -n openspot

# 이전 컨테이너 로그 확인 (크래시 시)
kubectl logs <pod-name> -n openspot --previous
```

### 포트 포워딩

```bash
# Gateway Service에 직접 접근
kubectl port-forward svc/gateway-service 8080:8080 -n openspot

# PostgreSQL에 직접 접근
kubectl port-forward svc/postgresql 5432:5432 -n openspot
```

### Shell 접속

```bash
# Pod에 Shell 접속
kubectl exec -it <pod-name> -n openspot -- /bin/sh

# PostgreSQL Shell 접속
kubectl exec -it postgresql-0 -n openspot -- psql -U postgres -d openspot
```

## 트러블슈팅

### Pod가 Pending 상태인 경우

```bash
# 상세 이벤트 확인
kubectl describe pod <pod-name> -n openspot

# Node 리소스 확인
kubectl top nodes
kubectl describe nodes

# 일반적인 원인과 해결책:
# 1. 리소스 부족: minikube 메모리/CPU 증가
# 2. PVC 대기: 스토리지 상태 확인 (kubectl get pvc -n openspot)
# 3. 이미지 다운로드 중: 시간이 더 필요함
```

### Pod가 CrashLoopBackOff 상태인 경우

```bash
# Pod 로그 확인 (최근 100줄)
kubectl logs <pod-name> -n openspot --tail=100

# 이전 컨테이너 로그 확인 (크래시 후)
kubectl logs <pod-name> -n openspot --previous

# Pod 이벤트 상세 보기
kubectl describe pod <pod-name> -n openspot

# PostgreSQL이 ready가 아닌 경우 (common)
kubectl logs postgresql-0 -n openspot --tail=50
```

### ImagePullBackOff 에러

```bash
# 이미지가 Minikube에 로드되었는지 확인
minikube image ls -p openspot | grep openspot

# 이미지 재빌드 및 로드
cd k8s/scripts
./3-build-images.sh

# Minikube 이미지 캐시 확인
minikube ssh -p openspot -- docker images | grep openspot
```

### PostgreSQL이 계속 unhealthy인 경우

```bash
# PostgreSQL Pod 직접 접속
kubectl exec -it postgresql-0 -n openspot -- psql -U postgres -d openspot

# 헬스체크 테스트
kubectl exec -it postgresql-0 -n openspot -- pg_isready -U postgres

# 초기화 완료 대기 (처음 시작 시 시간 필요)
# 로그에서 "database system is ready to accept connections" 확인
kubectl logs postgresql-0 -n openspot -f
```

### Service 연결 실패

```bash
# DNS 확인
kubectl run -it --rm debug --image=busybox --restart=Never -- nslookup postgresql.openspot.svc.cluster.local

# Service Endpoints 확인
kubectl get endpoints -n openspot

# Pod 간 통신 테스트
kubectl run -it --rm debug --image=busybox --restart=Never -- wget -O- http://config-service:9999
```

### Ingress 접속 실패

```bash
# Ingress Controller 상태 확인
kubectl get pods -n ingress-nginx

# /etc/hosts 확인
cat /etc/hosts | grep openspot.local

# Ingress 로그 확인
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller -f

# Minikube IP 확인
minikube ip -p openspot

# 직접 IP로 접속 테스트
curl -H "Host: openspot.local" http://$(minikube ip -p openspot)
```

### 리소스 부족 (Out of Memory)

```bash
# 현재 리소스 사용량 확인
kubectl top pods -n openspot --sort-by=memory

# Minikube 리소스 확인
minikube config get memory -p openspot
minikube config get cpus -p openspot

# 리소스 증가 (클러스터 재생성 필요)
minikube delete -p openspot
# minikube-config.sh에서 MEMORY=16384, CPUS=8로 수정
./2-create-cluster.sh
```

### StatefulSet Pod이 재시작되는 경우

PostgreSQL, Redis, Kafka 같은 StatefulSet은 디스크 I/O가 많아서:
- 초기 부팅 시 3-5분 소요 가능
- Pending/Initializing 상태가 정상
- 완전히 Ready가 될 때까지 기다리세요

```bash
# StatefulSet 상태 확인
kubectl get statefulset -n openspot
kubectl describe statefulset postgresql -n openspot

# 데이터 디렉토리 확인
kubectl exec -it postgresql-0 -n openspot -- ls -la /var/lib/postgresql/data/
```


## 다음 단계

### ✅ 완료된 작업

- ✅ Minikube 설정 스크립트 작성
- ✅ 배포 스크립트 최적화
- ✅ Helm values PostgreSQL 헬스체크 개선
- ✅ README 문서 완성
- ✅ 트러블슈팅 가이드 추가

### 🚀 배포 시작하기

```bash
cd k8s/scripts

# 1단계: 클러스터 생성
./2-create-cluster.sh

# 2단계: 이미지 빌드
./3-build-images.sh

# 3단계: Helm 배포
./4-deploy.sh

# 4단계: 상태 확인
kubectl get pods -n openspot
kubectl get svc -n openspot

# 5단계: 대시보드 확인
minikube dashboard -p openspot
```

### 📊 프로덕션 배포 준비 (향후)

1. **Secrets 암호화**: Sealed Secrets 또는 External Secrets Operator 사용
2. **모니터링**: Prometheus + Grafana 추가
3. **로깅**: EFK Stack (Elasticsearch + Fluentd + Kibana) 추가
4. **Auto-scaling**: HPA (Horizontal Pod Autoscaler) 설정
5. **Backup**: Velero로 백업 자동화
6. **클라우드 이관**: GKE, EKS, AKS 등으로 배포

## 참고 자료

- [Kubernetes 공식 문서](https://kubernetes.io/docs/)
- [Helm 공식 문서](https://helm.sh/docs/)
- [Minikube 공식 문서](https://minikube.sigs.k8s.io/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)

## 라이선스

이 프로젝트는 Open-Spot 프로젝트의 일부입니다.
