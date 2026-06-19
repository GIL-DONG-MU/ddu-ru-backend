package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 상세 헤더 정보")
public record ChatRoomInfoResponse(
        @Schema(description = "채팅방 ID", example = "10")
        Long chatRoomId,

        @Schema(description = "채팅방 타입. 타입에 따라 헤더 표시 필드가 달라집니다.", example = "GROUP", allowableValues = {"PRIVATE", "GROUP"})
        ChatRoomType roomType,

        @Schema(description = "채팅방 활성 여부. false이면 메시지 입력 비활성화 후보로 사용합니다.", example = "true")
        boolean isActive,

        @Schema(description = "PRIVATE 채팅방의 게시글 제목. GROUP은 null", example = "제주 애월 2박 3일", nullable = true)
        String postTitle,

        @Schema(description = "PRIVATE 채팅방 상대 닉네임. GROUP은 null", example = "여행메이트", nullable = true)
        String opponentNickname,

        @Schema(description = "GROUP 채팅방 여정 제목. PRIVATE은 null", example = "제주 애월 2박 3일 함께가기", nullable = true)
        String journeyTitle,

        @Schema(description = "GROUP 채팅방 활성 여정 멤버 수. PRIVATE은 null", example = "4", nullable = true)
        Integer memberCount
) {
}
