# API 설계 문서

Swagger보다 상세한 설계 배경, 처리 흐름, 테스트 케이스가 필요한 API 문서를 둡니다.

| 문서 | 한 줄 요약 |
|---|---|
| [API 설계 문서 템플릿](./000-template.md) | Swagger보다 상세한 API 설계 문서 작성 양식 |
| [채팅 메시지 조회 API](./001-chat-message-retrieve.md) | 커서 기반 메시지 조회, roomInfo, unreadCount 설계 |
| [채팅 읽음 처리 API](./002-chat-read-receipt.md) | lastReadMessageId 갱신, READ 이벤트, unreadCount 연동 설계 |
| [채팅방 목록 실시간 갱신](./003-chat-room-list-realtime.md) | 개인 WebSocket queue 기반 채팅방 목록 UPSERT/REMOVE 이벤트 설계 |

## 작성 기준

- 구현 전 설계 문서는 "권장/계획"과 "현재 구현"을 구분해서 씁니다.
- 구현이 완료되면 DTO, 예외, 테스트 목록을 실제 코드 기준으로 갱신합니다.
- Swagger에 노출되는 계약과 다르면 이 문서를 우선 수정합니다.
- 같은 폴더 안에서는 파일명과 문서 제목에 읽는 순서 숫자를 붙입니다. 예: `001-chat-message-retrieve.md`, `# 001. Chat Message Retrieve API Design`
- `000-template.md`는 작성 양식입니다. 실제 API 문서는 `001-*`부터 시작합니다.

## 템플릿

- [API 설계 문서 템플릿](./000-template.md)
