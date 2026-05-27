# ADR (Architecture Decision Records)

이 폴더는 DDU-RU Backend의 주요 기술/설계 의사결정을 기록합니다.
"무엇을 정했는가"는 설계 문서에, "왜 그렇게 정했는가"는 ADR에 남깁니다.

---

## 목록

| 번호 | 제목 | 상태 | 날짜 |
|:---:|---|:---:|---|
| 001 | [Package by Feature + Controller/Service/Repository 구조](./001-package-by-feature-controller-service-repository.md) | Proposed | TODO |
| 002 | [중앙 ErrorCode + 개별 BusinessException 클래스](./002-central-error-code-with-individual-exceptions.md) | Proposed | TODO |
| 003 | [OAuth JWT + Redis Refresh Token](./003-oauth-jwt-refresh-token-redis.md) | Proposed | TODO |
| 004 | [참여 신청과 나의 여정 멤버십 분리](./004-participation-journey-membership-separation.md) | Proposed | TODO |
| 005 | [ECR + Nginx Blue-Green 배포](./005-blue-green-deployment-with-nginx-ecr.md) | Proposed | TODO |

---

## 작성 규칙

- 파일명: `{번호}-{kebab-case-title}.md`
- 번호는 3자리, 순차 증가.
- 제목도 같은 번호를 붙여 `# ADR-001: 결정 제목` 형식으로 작성.
- 새 ADR 추가 시 위 표에 한 줄 추가.
- 폐기된 결정은 삭제하지 말고 상태를 `Deprecated` 또는 `Superseded by ADR-XXX`로 변경.
- 상태 종류: `Proposed` / `Accepted` / `Deprecated` / `Superseded by ADR-XXX`
- 코드에서 확인할 수 없는 결정자, 결정일, 배경은 추정하지 않고 `TODO`로 남김.

---

## 템플릿

ADR을 새로 추가할 때 아래 양식을 사용합니다.

```markdown
# ADR-XXX: 결정 제목

- 상태: Proposed
- 날짜: TODO
- 결정자: TODO

> TODO: 이 결정이 실제로 합의된 날짜, 참여자, 회의/PR/이슈 링크를 추가합니다.

## 맥락

어떤 상황에서 어떤 결정을 해야 했는지.

## 결정

무엇을 선택했는지.

## 대안

고려했지만 선택하지 않은 방법과 이유.

## 결과

좋은 점과 감수한 트레이드오프.

## 관련 문서

- TODO
```
