# 운영 / 인프라 / 배포 문서

로컬 개발 환경, 운영 인프라, 배포 파이프라인, DB 마이그레이션을 설명합니다.

| 문서 | 한 줄 요약 |
|---|---|
| [인프라 명세](./001-infrastructure.md) | MySQL/RDS, Redis, S3, CoolSMS, ECR, EC2, Nginx 구성 |
| [배포 파이프라인](./002-deployment.md) | GitHub Actions, ECR 이미지, EC2 blue-green 배포 |
| [Docker 운영 가이드](./003-docker.md) | 로컬 Docker Compose와 운영 Compose 실행 방법 |
| [Flyway 가이드](./004-flyway.md) | 운영 DB 마이그레이션 적용 방식 |

> `deployment/` 폴더는 Nginx 운영 설정 파일을 담고, `docs/004-operations/`는 그 설정을 설명합니다.

## 파일명 / 제목 규칙

- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다.
- 예: `001-infrastructure.md`, `# 001. 인프라 명세`

## 운영 문서 양식

인프라, 배포, 마이그레이션, 장애 대응 절차를 설명할 때 사용합니다.

````markdown
# 001. 운영 문서 제목

> 이 문서를 보면 어떤 운영 절차나 인프라 구성을 이해할 수 있는지 설명합니다.

---

## 한 줄 요약

운영 관점의 핵심 결론을 한 문장으로 씁니다.

---

## 1. 구성 요소

| 구성 | 역할 | 위치/설정 |
|---|---|---|
| TODO | TODO | TODO |

## 2. 실행 / 배포 절차

```bash
TODO
```

## 3. 환경변수 / Secret

| 이름 | 필수 | 설명 |
|---|:---:|---|
| TODO | Y | TODO |

## 4. 확인 방법

```bash
TODO
```

## 5. 장애 대응

| 증상 | 먼저 볼 곳 | 조치 |
|---|---|---|
| TODO | TODO | TODO |

## 6. 한계 / 개선 후보

- TODO

## 관련 문서

- TODO
````
