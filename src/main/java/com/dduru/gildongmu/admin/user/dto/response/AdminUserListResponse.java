package com.dduru.gildongmu.admin.user.dto.response;

import com.dduru.gildongmu.user.domain.User;
import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserListResponse(
        List<AdminUserSummaryResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        boolean last
) {
    public static AdminUserListResponse from(Page<User> page) {
        return new AdminUserListResponse(
                page.getContent().stream().map(AdminUserSummaryResponse::from).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isLast()
        );
    }
}
