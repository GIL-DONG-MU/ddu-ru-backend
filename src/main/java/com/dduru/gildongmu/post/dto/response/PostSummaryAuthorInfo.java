package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record PostSummaryAuthorInfo(
        @Schema(description = "작성자 닉네임", example = "여행메이트")
        String nickname,
        @Schema(description = "작성자가 슈퍼호스트인지 여부", example = "false")
        boolean isSuperHost
) {
    public static PostSummaryAuthorInfo from(User user, boolean isSuperHost) {
        Profile profile = user.getProfile();
        return new PostSummaryAuthorInfo(profile.getNickname(), isSuperHost);
    }
}
