package com.dduru.gildongmu.auth.dto;

public record NicknameUpdateResponse(
        String message,
        String nickname
) {
    public static NicknameUpdateResponse of(String nickname) {
        return new NicknameUpdateResponse("닉네임이 성공적으로 수정되었습니다.", nickname);
    }
}