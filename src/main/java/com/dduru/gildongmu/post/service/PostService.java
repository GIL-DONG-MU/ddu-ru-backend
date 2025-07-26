package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.repository.UserRepository;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostCreateRequest;
import com.dduru.gildongmu.post.dto.PostCreateResponse;
import com.dduru.gildongmu.post.enums.Destination;
import com.dduru.gildongmu.post.repository.DestinationRepository;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final JsonConverter jsonConverter;
    public PostCreateResponse createPost(Long userId,PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다"));

        Destination destination = destinationRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DESTINATION_NOT_FOUND, "여행지를 찾을 수 없습니다"));

        Gender preferredGender = request.getPreferredGender() != null
                ? Gender.from(request.getPreferredGender())
                : Gender.U;

        AgeRange preferredAge = request.getPreferredAge() != null
                ? AgeRange.from(request.getPreferredAge())
                : AgeRange.UNKNOWN;

        String photoUrlsJson = jsonConverter.convertListToJson(request.getPhotoUrls());
        String tagsJson = jsonConverter.convertListToJson(request.getTags());

        Post post = Post.builder()
                .user(user)
                .destination(destination)
                .title(request.getTitle())
                .content(request.getContent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .recruitCapacity(request.getRecruitCapacity())
                .recruitDeadline(request.getRecruitDeadline())
                .preferredGender(preferredGender)
                .preferredAge(preferredAge)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .photoUrls(photoUrlsJson)
                .tags(tagsJson)
                .build();

        Post savedPost = postRepository.save(post);

        return PostCreateResponse.from(
                savedPost,
                request.getPhotoUrls(),
                request.getTags()
        );
    }
}
