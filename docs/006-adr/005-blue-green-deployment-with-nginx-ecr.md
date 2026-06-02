# ADR-005: ECR + Nginx Blue-Green 배포

- 상태: Proposed
- 날짜: TODO
- 결정자: TODO

> TODO: ECR, EC2 Docker Compose, Nginx blue-green을 선택한 운영 배경과 대안 비교 근거를 추가합니다.

## 맥락

현재 CD는 GitHub Actions에서 Docker 이미지를 AWS ECR에 push하고, EC2에 배포 번들을 복사한 뒤 `deploy.sh`를 실행합니다. 운영 Compose에는 `app_blue`, `app_green`, `nginx`, `redis`가 정의되어 있습니다.

## 결정

EC2 Docker Compose 환경에서 두 app container 색상 중 idle 색상에 새 버전을 띄우고, readiness 통과 후 Nginx upstream include 파일을 교체해 트래픽을 전환합니다.

## 대안

| 대안 | 기각/보류 이유 |
|---|---|
| 단일 app container 재기동 | 배포 중 다운타임이 발생할 수 있음 |
| ECS/ALB 기반 blue-green | 운영 인프라 복잡도와 비용이 커질 수 있음 |
| Git pull 후 서버 빌드 | 서버 빌드 시간이 길고 환경 차이 위험이 큼 |

## 결과

- readiness가 통과한 container로만 트래픽을 전환할 수 있습니다.
- Nginx 설정 검증 실패 시 이전 upstream으로 복구합니다.
- readiness 또는 Nginx 전환 실패 시 app/nginx 최근 로그를 출력합니다.
- EC2와 Docker Compose에 운영 책임이 집중되므로 로그/디스크/인증서 관리 runbook이 필요합니다.

## 관련 문서

- [인프라 명세](../004-operations/001-infrastructure.md)
- [배포 파이프라인](../004-operations/002-deployment.md)
