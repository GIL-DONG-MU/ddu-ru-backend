# 001. 팀 협업 컨벤션

> 이 문서를 보면 이슈, 브랜치, 커밋, PR을 어떤 규칙으로 작성하는지 파악할 수 있습니다.

---

## 한 줄 요약

이슈 템플릿을 먼저 작성하고, `feat/개발내용` 형태의 브랜치에서 작업한 뒤, `[#이슈번호] feat(scope): 커밋 내용` 형식의 커밋과 `feat(scope): PR 제목` 형식의 PR 체크리스트로 리뷰합니다.

---

## 1. Issue

템플릿 위치:

- `.github/ISSUE_TEMPLATE/✨-feature-template.md`
- `.github/ISSUE_TEMPLATE/🐞-bug-template.md`
- `.github/ISSUE_TEMPLATE/⚒️-refactor-template.md`
- `.github/ISSUE_TEMPLATE/🧩-chore-template.md`
- `.github/ISSUE_TEMPLATE/📝-task-template.md`

기능 이슈에는 API 명세, 요청/응답, 예외 처리, 완료 조건을 함께 작성합니다.

---

## 2. Branch

PR 템플릿 기준:

```text
feat/개발내용
```

예:

- `feat/post-search`
- `feat/chat-read-receipt`
- `fix/oauth-refresh-token`
- `docs/reorganize-docs`

---

## 3. Commit

PR 템플릿 기준:

```text
[#이슈번호] feat(scope): 커밋 내용
```

예:

```text
[#123] feat(post): 게시글 검색 필터 추가
[#124] fix(auth): 토큰 재발급 예외 처리 보강
[#125] docs(swagger): 응답 스키마 설명 보강
```

권장 type:

| Type | 설명 |
|---|---|
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |
| `docs` | 문서 변경 |
| `test` | 테스트 추가/수정 |
| `chore` | 설정/빌드/운영 보조 작업 |

하나의 커밋에는 하나의 관심사를 담고, 이슈 기반 작업은 커밋 메시지 앞에 연결된 이슈 번호를 `[#이슈번호]` 형식으로 붙입니다.

---

## 4. Pull Request

템플릿 위치:

- `.github/PULL_REQUEST_TEMPLATE.md`

PR 제목 형식:

```text
feat(scope): PR 제목
```

예:

```text
feat(post): 게시글 검색 필터 추가
fix(auth): 토큰 재발급 예외 처리 보강
docs(swagger): 응답 스키마 설명 보강
```

PR 본문 작성 방식:

| 항목 | 규칙 |
|---|---|
| 작업 내용 | `했습니다`, `없습니다` 같은 문장형 종결 대신 명사형·종결 생략 bullet로 작성 |
| 이슈 링크 | 이슈 번호만 작성 |
| 스크린샷 | 없으면 `없음`으로 작성 |
| 리뷰 요구사항 | 리뷰어에게 필요한 확인 지점을 자유롭게 작성 |
| 예정 작업 | 없으면 `없음`으로 작성 |

예:

```text
* 응답 코드 문서화
* DTO Schema 설명 보강
* 인증 오류 응답 검증
```

PR 체크리스트 핵심:

- 작업 전 Issue 작성.
- 브랜치 이름 확인.
- 커밋 메시지 형식 확인.
- PR 제목 형식 확인.
- PR 본문 명사형·종결 생략 작성 확인.
- `dev` 브랜치로 병합 요청.
- 불필요한 주석/공백 제거.
- 테스트 진행 및 테스트 코드 작성 여부 확인.

---

## 5. 문서 변경 규칙

- API/DB/배포 정책이 바뀌면 관련 문서를 같은 PR에서 수정합니다.
- 새 문서를 추가하면 상위 폴더 `README.md`에 링크를 추가합니다.
- 코드에서 확인할 수 없는 의사결정 배경은 ADR에 `TODO`로 남깁니다.
