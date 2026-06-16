package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findPostsWithFilters(PostListRequest request, LocalDate today, Integer cursorValue, Pageable pageable);
}
