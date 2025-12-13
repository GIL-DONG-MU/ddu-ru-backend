package com.dduru.gildongmu.nickname.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "닉네임 생성 응답")
@Builder
public record NicknameGenerateResponse(

        @Schema(description = "생성된 닉네임 목록", example = "[\"용감한 모험가\", \"즐거운 여행자\", \"행복한 탐험가\"]")
        List<String> nicknames,

        @Schema(description = "생성된 닉네임 개수", example = "5")
        int count

) {
    public static NicknameGenerateResponse of(List<String> nicknames) {
        return NicknameGenerateResponse.builder()
                .nicknames(nicknames)
                .count(nicknames.size())
                .build();
    }
}
