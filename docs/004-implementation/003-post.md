# 003. 모집글 구현 문서

> 모집글 목록/상세 응답, 검색/정렬, 생성/수정/삭제, 자동 마감 구현 세부를 정리합니다.
> 제품 흐름은 [모집글 제품 흐름](../001-policy/flows/001-post-product-flow.md)을 참고합니다.

---

## 1. 목록 조회

### 필터 / 정렬 조건

| 파라미터 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `keyword` | String | - | 제목, 내용, 여행지 도시/국가, 태그 검색 |
| `startDate` | LocalDate | - | 여행 시작일 이후 필터 |
| `endDate` | LocalDate | - | 여행 종료일 이전 필터 |
| `preferredGender` | Gender | - | 선호 성별 필터. `F`/`M` 선택 시 무관(`U`) 게시글 포함 |
| `minAge` | Integer | - | 선호 연령 최솟값. 게시글 나이 범위와 겹치는 게시글 반환 |
| `maxAge` | Integer | - | 선호 연령 최댓값 |
| `destinationId` | Long | - | 여행지 필터 |
| `recruitmentStatus` | RecruitmentStatusFilter | - | 모집 상태 필터 |
| `companionType` | CompanionType | - | 동행 방식 필터 |
| `sort` | PostSortType | `LATEST` | 정렬 조건 |
| `cursor` | Long | - | 커서 기반 페이지네이션. 직전 마지막 항목의 postId |
| `cursorValue` | Integer | - | 조회순/좋아요순 보조 커서 값. 서버가 내려준 `nextCursorValue`를 다음 요청에 그대로 전달 |
| `size` | Integer | `10` | 페이지 크기. 최대 50 |

### 모집 상태 필터

`recruitmentStatus`를 전달하지 않으면 `OPEN`과 동일하게 동작합니다.

| 값 | 조건 |
| --- | --- |
| `OPEN` | status=OPEN AND 인원 여유 있음 |
| `DEADLINE_NEAR` | status=OPEN AND 인원 여유 있음 AND 모집 마감일이 오늘~3일 이내 |
| `CLOSED` | 인원 마감 OR 호스트/자동 마감 |

### 정렬 조건

| 값 | 설명 | 정렬 기준 |
| --- | --- | --- |
| `LATEST` | 최신순 | `id DESC` |
| `VIEW` | 조회순 | `viewCount DESC, id DESC` |
| `LIKE` | 좋아요순 | `likeCount DESC, id DESC` |

동점 정렬 기준으로 항상 `id DESC`를 추가해 순서 안정성을 보장합니다.

### 커서 기반 페이지네이션

`cursor`가 없으면 첫 페이지, 있으면 해당 cursor 이후 데이터를 반환합니다.
`size + 1`개를 조회해 `hasNext` 여부를 결정합니다.

| sort | 커서 조건 |
| --- | --- |
| `LATEST` | `id < cursorId` |
| `VIEW` | `viewCount < cursorViewCount OR (viewCount = cursorViewCount AND id < cursorId)` |
| `LIKE` | `likeCount < cursorLikeCount OR (likeCount = cursorLikeCount AND id < cursorId)` |

`VIEW`, `LIKE` 정렬에서는 `cursor`와 함께 서버가 내려준 `cursorValue`를 전달받아 복합 조건으로 사용합니다.

### PostSummaryResponse

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | Long | 게시글 ID |
| `title` | String | 제목 |
| `status` | PostStatus | 모집 상태 |
| `isFull` | Boolean | 정원 초과 여부 |
| `startDate` | LocalDate | 여행 시작일 |
| `endDate` | LocalDate | 여행 종료일 |
| `destination` | String | 여행지 도시명 |
| `recruitCapacity` | Integer | 모집 정원 |
| `recruitCount` | Integer | 현재 승인 인원 |
| `preferredGender` | Gender | 선호 성별 |
| `companionType` | CompanionType | 동행 방식 |
| `photoUrl` | String | 대표 사진 URL |
| `likeCount` | Integer | 좋아요 수 |
| `hasLiked` | Boolean | 현재 사용자의 좋아요 여부 |
| `author` | PostAuthorInfo | 작성자 정보 |

### hasLiked 처리

- 비로그인 요청(`userId = null`): 항상 `false`
- 로그인 요청: 페이지 내 모든 게시글 ID를 한 번에 IN 쿼리로 좋아요 여부를 벌크 조회

```sql
SELECT pl.post_id
FROM post_likes pl
WHERE pl.user_id = :userId
  AND pl.post_id IN (:postIds)
```

### isSuperHost 처리

페이지 내 모든 게시글 ID를 한 번에 IN 쿼리로 활성 슈퍼호스트 노출 여부를 벌크 조회합니다.
조회 결과는 `author.isSuperHost` 필드로 반환합니다.

```sql
SELECT e.post_id
FROM super_host_exposures e
WHERE e.post_id IN (:postIds)
  AND e.status = 'ACTIVE'
  AND e.ended_at > :now
```

---

## 2. 상세 조회

상세 조회 시 조회수(`viewCount`)가 1 증가합니다.

응답에는 아래가 포함됩니다.

- 기본 정보: 제목, 내용, 여행지, 일정, 모집 조건
- 파생값: `tripDurationText`, `recruitDeadlineDDay`
- 권한값: `isOwner`, `canEditPost`
- 참여 정보: `participants`, `myParticipationStatus`
- 좋아요: `hasLiked`

### PostDetailResponse 주요 항목

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `companionType` | CompanionType | 동행 방식 |
| `author` | PostAuthorInfo | 작성자 상세 정보 |
| `participants` | List&lt;ParticipantInfo&gt; | 승인된 참여자 목록 |
| `hasLiked` | Boolean | 현재 사용자의 좋아요 여부 |
| `isOwner` | Boolean | 현재 사용자가 호스트인지 여부 |
| `canEditPost` | Boolean | 수정 가능 여부 |
| `myParticipationStatus` | MyParticipationStatus | 현재 사용자의 신청 상태 |

### PostAuthorInfo

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | 작성자 ID |
| `nickname` | String | 닉네임 |
| `profileImage` | ProfileImageInfo | 프로필 이미지 |
| `gender` | Gender | 성별 |
| `ageGroup` | Integer | 연령대 |
| `isSuperHost` | Boolean | 슈퍼호스트 노출 중 여부 |

### ParticipantInfo

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | 참여자 ID |
| `nickname` | String | 닉네임 |
| `profileImage` | ProfileImageInfo | 프로필 이미지 |
| `isHost` | Boolean | 호스트 여부 |
| `gender` | Gender | 성별 |
| `ageGroup` | Integer | 연령대 |

### canEditPost 계산

```text
isOwner == true
AND 모집 마감일이 지나지 않음
AND 여행이 시작되지 않음
AND 여행이 끝나지 않음
```

여행 시작일 당일부터 `canEditPost = false`입니다.

### myParticipationStatus

| 값 | 설명 |
| --- | --- |
| `NONE` | 신청한 이력 없음. 호스트 포함 |
| `PENDING` | 신청 완료, 방장 검토 대기 |
| `CONTACTING` | 방장이 1:1 채팅으로 연락 시작 |
| `APPROVED` | 수락 완료 |
| `REJECTED` | 거절됨 |
| `REMOVED_BY_HOST` | 승인 후 방장에게 내보내짐 |

---

## 3. 생성 / 수정 / 삭제

### 생성 시 자동 처리

```text
POST /api/v1/posts
    ↓
Post 저장
    ↓
Journey.create(post) 저장
    ↓
JourneyMember.createHost(journey, user) 저장
    ↓
GroupChatRoom 생성
```

### 주요 검증

- `title`: 5~40자
- `content`: 20~1000자
- `endDate >= startDate`
- `isAgeAny == true`이면 `minAge`, `maxAge`는 null
- `isAgeAny == false`이면 `minAge`, `maxAge` 모두 필수. 20~100 범위
- 태그는 최대 4개
- `recruitCapacity`는 2~10명. 호스트 포함

### recruitDeadline 자동 계산

```text
recruitDeadline = endDate - 1일
```

### 수정

- 요청자가 호스트(`posts.user_id == userId`)여야 합니다.
- `recruitDeadline`이 지나지 않아야 합니다.
- 여행이 시작되거나 끝나지 않아야 합니다.
- `endDate`가 변경되면 `recruitDeadline`도 함께 재계산합니다.
- `recruitCapacity`를 줄일 때는 현재 `recruitCount`보다 작게 설정할 수 없습니다.

### 삭제

- 소프트 삭제(`isDeleted = true`)로 처리합니다.
- 삭제 시 슈퍼호스트 활성 노출을 함께 취소합니다.
- 삭제된 게시글에서도 기존 그룹 채팅방은 유지합니다.

---

## 4. 모집 상태와 자동 마감

호스트가 직접 `OPEN` / `CLOSED`를 전환할 수 있습니다.

| 전환 방향 | 부수 효과 |
| --- | --- |
| `OPEN -> CLOSED` | 슈퍼호스트 활성 노출 취소 |
| `CLOSED -> OPEN` | 없음 |

자동 마감 스케줄러는 매일 자정(`0 0 0 * * *`)에 실행됩니다.

```text
모집 마감일(recruitDeadline) < 오늘인 OPEN 게시글
    -> status = CLOSED 일괄 업데이트
    -> 슈퍼호스트 활성 노출 일괄 취소
```
