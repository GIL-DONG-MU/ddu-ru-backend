package com.dduru.gildongmu.superhost.dto.response;

import java.time.LocalDateTime;

public record MySuperHostStatusResponse(
        int unusedTicketCount,
        boolean hasActive,
        Long activePostId,
        LocalDateTime activeEndsAt
) {
    public static MySuperHostStatusResponse of(
            int unusedTicketCount,
            boolean hasActive,
            Long activePostId,
            LocalDateTime activeEndsAt
    ) {
        return new MySuperHostStatusResponse(unusedTicketCount, hasActive, activePostId, activeEndsAt);
    }
}
