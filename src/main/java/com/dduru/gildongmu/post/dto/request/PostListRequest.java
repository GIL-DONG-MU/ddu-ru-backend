package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.PostSortType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PostListRequest(
        Long cursor,
        Integer size,
        String keyword,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Gender preferredGender,
        Integer preferredAge,
        Long destinationId,
        Boolean isRecruitOpen,
        PostSortType sort
) {
    public PostListRequest {
        if (size == null || size <= 0 || size > 50) size = 10;
        if (sort == null) sort = PostSortType.LATEST;
    }
}
