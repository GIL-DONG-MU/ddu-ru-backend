package com.dduru.gildongmu.nickname.dto;

import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "닉네임 생성 요청")
public record NicknameGenerateRequest(

        @Schema(description = "생성할 닉네임 개수", example = "5", defaultValue = "5")
        @Min(value = 1, message = "최소 1개 이상 생성해야 합니다.")
        @Max(value = 10, message = "한 번에 최대 10개까지 생성할 수 있습니다.")
        Integer count,

        @Schema(description = "닉네임 테마", example = "TRAVEL", defaultValue = "RANDOM")
        NicknameTheme theme,

        @Schema(description = "숫자 접미사 추가 여부", example = "false", defaultValue = "false")
        Boolean includeNumber

) {
    public NicknameGenerateRequest {
        if (count == null) {
            count = 5;
        }
        if (theme == null) {
            theme = NicknameTheme.RANDOM;
        }
        if (includeNumber == null) {
            includeNumber = false;
        }
    }

    public static NicknameGenerateRequest defaultRequest() {
        return new NicknameGenerateRequest(5, NicknameTheme.RANDOM, false);
    }
}
