package com.dduru.gildongmu.auth.dto;

import com.dduru.gildongmu.user.domain.User;

public record UserCreationResult(
        User user,
        boolean isNewUser
) {
}
