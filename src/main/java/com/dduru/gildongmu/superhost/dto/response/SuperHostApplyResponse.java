package com.dduru.gildongmu.superhost.dto.response;

import java.time.LocalDateTime;

public record SuperHostApplyResponse(
        Long postId,
        Long ticketId,
        LocalDateTime startedAt,
        LocalDateTime endsAt,
        int unusedTicketCount
) {
    public static SuperHostApplyResponse of(
            Long postId,
            Long ticketId,
            LocalDateTime startedAt,
            LocalDateTime endsAt,
            int unusedTicketCount
    ) {
        return new SuperHostApplyResponse(postId, ticketId, startedAt, endsAt, unusedTicketCount);
    }
}
