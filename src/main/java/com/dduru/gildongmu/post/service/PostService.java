package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.PostCreateRequest;
import com.dduru.gildongmu.post.dto.PostCreateResponse;
import com.dduru.gildongmu.post.dto.PostUpdateRequest;
import com.dduru.gildongmu.S3.exception.ImageCountExceededException;
import com.dduru.gildongmu.post.exception.InvalidAgeRangeException;
import com.dduru.gildongmu.post.exception.InvalidBudgetRangeException;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.S3.service.S3Service;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private static final int MAX_IMAGE_COUNT = 3;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final JsonConverter jsonConverter;
    private final S3Service s3Service;

    public PostCreateResponse create(Long userId, PostCreateRequest request, List<MultipartFile> images) {
        log.debug("게시글 생성 시작 - userId: {}, request: {}", userId, request);

        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(), request.preferredAgeMin(), request.preferredAgeMax());
        
        User user = userRepository.getByIdOrThrow(userId);
        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());
        
        List<String> uploadedImageUrls = uploadImages(images);
        
        Post post = createPost(user, destination, request, uploadedImageUrls);
        Post savedPost = postRepository.save(post);

        log.info("게시글 생성 완료 - postId: {}, userId: {}, title: {}, 이미지 개수: {}",
                savedPost.getId(), userId, savedPost.getTitle(), uploadedImageUrls.size());
        return new PostCreateResponse(savedPost.getId());
    }

    public void update(Long postId, Long userId, PostUpdateRequest request, List<MultipartFile> images) {
        log.debug("게시글 수정 시작 - postId: {}, userId: {}, request: {}", postId, userId, request);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);
        validateBusinessRules(request.startDate(), request.endDate(), request.recruitDeadline(),
                request.budgetMin(), request.budgetMax(), request.preferredAgeMin(), request.preferredAgeMax());

        Destination destination = destinationRepository.getByIdOrThrow(request.destinationId());
        List<String> uploadedImageUrls = uploadImages(images);
        
        updatePost(post, destination, request, uploadedImageUrls);

        log.info("게시글 수정 완료 - postId: {}, userId: {}, title: {}, 이미지 개수: {}",
                postId, userId, post.getTitle(), uploadedImageUrls.size());
    }

    public void delete(Long postId, Long userId) {
        log.debug("게시글 삭제 시작 - postId: {}, userId: {}", postId, userId);

        Post post = postRepository.getActiveByIdOrThrow(postId);
        validatePermission(post, userId);

        post.softDelete(userId);

        log.info("게시글 삭제 완료 - postId: {}, userId: {}, title: {}",
                postId, userId, post.getTitle());
    }

    private void validatePermission(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            log.warn("게시글 권한 없음 - postId: {}, userId: {}, ownerId: {}", post.getId(), userId, post.getUser().getId());
            throw PostAccessDeniedException.ownerOnly();
        }
    }

    private void validateBusinessRules(LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline,
                                       Integer budgetMin, Integer budgetMax,
                                       String preferredAgeMin, String preferredAgeMax) {

        if (endDate.isBefore(startDate)) {
            throw InvalidPostDateException.endBeforeStart();
        }
        if (recruitDeadline.isAfter(startDate)) {
            throw InvalidPostDateException.deadlineAfterStart();
        }
        if (budgetMin != null && budgetMax != null && budgetMax < budgetMin) {
            throw new InvalidBudgetRangeException();
        }
        if (preferredAgeMin != null && preferredAgeMax != null) {
            validateAgeRange(preferredAgeMin, preferredAgeMax);
        }
    }

    private void validateAgeRange(String ageMin, String ageMax) {
        try {
            AgeRange minAge = AgeRange.from(ageMin);
            AgeRange maxAge = AgeRange.from(ageMax);
            if (minAge.ordinal() > maxAge.ordinal()) {
                throw InvalidAgeRangeException.maxLessThanMin();
            }
        } catch (IllegalArgumentException e) {
            throw InvalidAgeRangeException.invalidValue();
        }
    }

    public int closeExpiredPosts() {
        log.debug("만료된 게시글 상태 업데이트 시작");
        
        LocalDate today = LocalDate.now();
        List<Post> expiredPosts = postRepository.findExpiredOpenPosts(today);
        
        int updatedCount = 0;
        for (Post post : expiredPosts) {
            post.closeByRecruitmentDeadline();
            updatedCount++;
            log.debug("게시글 상태 CLOSED로 변경 - postId: {}, title: {}", post.getId(), post.getTitle());
        }
        
        log.info("만료된 게시글 {}개의 상태를 CLOSED로 변경 완료", updatedCount);
        return updatedCount;
    }


    private Post createPost(User user, Destination destination, PostCreateRequest request, List<String> uploadedImageUrls) {
        ParsedPostData parsed = parsePostData(request.preferredGender(), request.preferredAgeMin(), 
                request.preferredAgeMax(), uploadedImageUrls, request.tags(), destination);
        
        return Post.createPost(user, destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                request.recruitDeadline(), parsed.preferredGender(), parsed.preferredAgeMin(), 
                parsed.preferredAgeMax(), request.budgetMin(), request.budgetMax(),
                parsed.photoUrlsJson(), parsed.tagsJson());
    }

    private void updatePost(Post post, Destination destination, PostUpdateRequest request, List<String> uploadedImageUrls) {
        ParsedPostData parsed = parsePostData(request.preferredGender(), request.preferredAgeMin(),
                request.preferredAgeMax(), uploadedImageUrls, request.tags(), destination);
        
        post.updatePost(destination, request.title(), request.content(),
                request.startDate(), request.endDate(), request.recruitCapacity(),
                request.recruitDeadline(), parsed.preferredGender(), parsed.preferredAgeMin(),
                parsed.preferredAgeMax(), request.budgetMin(), request.budgetMax(),
                parsed.photoUrlsJson(), parsed.tagsJson());
    }

    private ParsedPostData parsePostData(String preferredGender, String preferredAgeMin, 
                                       String preferredAgeMax, List<String> photoUrls, List<String> tags, Destination destination) {
        
        List<String> finalPhotoUrls = getFinalPhotoUrls(photoUrls, destination);
        
        return new ParsedPostData(
                preferredGender != null ? Gender.from(preferredGender) : Gender.U,
                preferredAgeMin != null ? AgeRange.from(preferredAgeMin) : null,
                preferredAgeMax != null ? AgeRange.from(preferredAgeMax) : null,
                jsonConverter.convertListToJson(finalPhotoUrls),
                jsonConverter.convertListToJson(tags)
        );
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            log.debug("업로드할 이미지 없음");
            return Collections.emptyList();
        }

        if (images.size() > MAX_IMAGE_COUNT) {
            throw ImageCountExceededException.withMax(MAX_IMAGE_COUNT);
        }

        log.debug("이미지 업로드 시작 - 개수: {}", images.size());
        return s3Service.uploadFiles(images);
    }
    
    private List<String> getFinalPhotoUrls(List<String> photoUrls, Destination destination) {
        if (photoUrls != null && !photoUrls.isEmpty()) {
            log.debug("사용자가 업로드한 이미지 사용 - 개수: {}", photoUrls.size());
            return photoUrls;
        }
        
        if (StringUtils.hasText(destination.getImage())) {
            log.debug("목적지 기본 이미지 사용 - destination: {}, image: {}", 
                    destination.getCity(), destination.getImage());
            List<String> defaultImages = new ArrayList<>();
            defaultImages.add(destination.getImage());
            return defaultImages;
        }
        
        log.debug("이미지 없음 - 빈 리스트 반환");
        return Collections.emptyList();
    }

    private record ParsedPostData(
            Gender preferredGender,
            AgeRange preferredAgeMin,
            AgeRange preferredAgeMax,
            String photoUrlsJson,
            String tagsJson
    ) {
    }
}
