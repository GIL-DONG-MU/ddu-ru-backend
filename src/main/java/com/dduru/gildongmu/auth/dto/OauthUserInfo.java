package com.dduru.gildongmu.auth.dto;

import com.dduru.gildongmu.user.enums.OauthType;
import lombok.Builder;

@Builder
public record OauthUserInfo(
        String oauthId,
        String email,
        String name,
        OauthType loginType
        // 회원가입 시 기본 정보만 받으므로 추가 정보 필드는 사용하지 않음
        // String profileImage,
        // String gender,  // 사용하지 않음 (항상 null이었으므로 제거)
        // String phoneNumber  // 사용하지 않음 (항상 null이었으므로 제거)
        // String ageRange  // User에서 사용하지 않으므로 제거 (Post에서만 사용)
) {
}
