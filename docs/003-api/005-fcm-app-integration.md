# 005. FCM 앱 연동 가이드

> 백엔드 푸시 알림이 정상 동작하려면 앱에서 아래 3가지를 구현해야 한다.

---

## 1. Firebase 설정 파일

Firebase 콘솔에서 프로젝트를 공유받아 플랫폼별 설정 파일을 다운로드한다.

| 플랫폼 | 파일 | 위치 |
|---|---|---|
| Android | `google-services.json` | 앱 모듈 루트 (`app/`) |
| iOS | `GoogleService-Info.plist` | Xcode 프로젝트 루트 |

> Firebase 콘솔 접근 권한은 백엔드팀에 요청한다.

---

## 2. 구현해야 할 작업

### 2-1. 로그인 성공 후 토큰 등록

로그인 완료 시 Firebase SDK에서 현재 토큰을 가져와 서버에 등록한다.

```kotlin
// Android
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    api.registerFcmToken(token, deviceType = "AOS")
}
```

```swift
// iOS
Messaging.messaging().token { token, error in
    guard let token = token else { return }
    api.registerFcmToken(token, deviceType: "IOS")
}
```

### 2-2. 토큰 갱신 콜백 구현 ← 누락되기 쉬운 부분

Firebase가 토큰을 새로 발급할 때 자동 호출되는 콜백에서 서버에 재등록한다.
이를 구현하지 않으면 앱 재설치 후 푸시가 유실된다.

```kotlin
// Android - FirebaseMessagingService 상속 클래스
override fun onNewToken(token: String) {
    api.registerFcmToken(token, deviceType = "AOS")
}
```

```swift
// iOS - AppDelegate
func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
    guard let token = fcmToken else { return }
    api.registerFcmToken(token, deviceType: "IOS")
}
```

### 2-3. 로그아웃 시 토큰 삭제

로그아웃 전에 현재 토큰을 서버에서 삭제한다.
삭제하지 않으면 로그아웃 상태에서도 푸시가 수신된다.

```kotlin
// Android
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    api.deleteFcmToken(token)
    // 완료 후 로그아웃 API 호출
}
```

```swift
// iOS
Messaging.messaging().token { token, error in
    guard let token = token else { return }
    api.deleteFcmToken(token)
    // 완료 후 로그아웃 API 호출
}
```

---

## 3. API 스펙

모든 API는 JWT 인증 헤더가 필요하다.

### 토큰 등록/갱신

```
POST /api/v1/fcm/token
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "token": "eKxy3z1...",
  "deviceType": "AOS"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `token` | String | Y | Firebase FCM 디바이스 토큰 |
| `deviceType` | String | Y | 기기 타입. `AOS` \| `IOS` |

- 응답: `204 No Content`
- 같은 기기 타입으로 이미 토큰이 있으면 갱신한다.
- 같은 토큰이 다른 계정에 등록된 경우 자동으로 소유권이 이전된다.

### 토큰 삭제

```
DELETE /api/v1/fcm/token
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "token": "eKxy3z1..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `token` | String | Y | 삭제할 FCM 디바이스 토큰 |

- 응답: `204 No Content`
