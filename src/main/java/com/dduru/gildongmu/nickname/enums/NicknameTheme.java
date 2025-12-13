package com.dduru.gildongmu.nickname.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 테마")
public enum NicknameTheme {

    @Schema(description = "랜덤 (모든 테마에서 선택)")
    RANDOM,

    @Schema(description = "여행 테마")
    TRAVEL,

    @Schema(description = "자연 테마")
    NATURE,

    @Schema(description = "동물 테마")
    ANIMAL,

    @Schema(description = "음식 테마")
    FOOD,

    @Schema(description = "우주 테마")
    SPACE,

    @Schema(description = "판타지 테마")
    FANTASY
}
