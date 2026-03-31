package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.user.domain.User;

import java.util.List;

public record InviteTargetsResult(
        List<User> newGuests,
        List<Long> missingUserIds,
        List<Long> alreadyMemberUserIds
) {
}
