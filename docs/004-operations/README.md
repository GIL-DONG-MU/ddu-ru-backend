# 운영 / 인프라 / 배포 문서

로컬 개발 환경, 운영 인프라, 배포 파이프라인, DB 마이그레이션을 설명합니다.

| 문서 | 한 줄 요약 |
|---|---|
| [운영 문서 템플릿](./000-template.md) | 인프라, 배포, 마이그레이션, 장애 대응 문서 작성 양식 |
| [인프라 명세](./001-infrastructure.md) | MySQL/RDS, Redis, S3, CoolSMS, ECR, EC2, Nginx 구성 |
| [배포 파이프라인](./002-deployment.md) | GitHub Actions, ECR 이미지, EC2 blue-green 배포 |
| [Docker 운영 가이드](./003-docker.md) | 로컬 Docker Compose와 운영 Compose 실행 방법 |
| [Flyway 가이드](./004-flyway.md) | 운영 DB 마이그레이션 적용 방식 |

> `deployment/` 폴더는 Nginx 운영 설정 파일을 담고, `docs/004-operations/`는 그 설정을 설명합니다.

## 파일명 / 제목 규칙

- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다.
- 예: `001-infrastructure.md`, `# 001. 인프라 명세`
- `000-template.md`는 작성 양식입니다. 실제 운영 문서는 `001-*`부터 시작합니다.

## 템플릿

- [운영 문서 템플릿](./000-template.md)
