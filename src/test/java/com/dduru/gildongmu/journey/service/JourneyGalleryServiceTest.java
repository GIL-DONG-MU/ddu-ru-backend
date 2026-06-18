package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.JourneyPostImage;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyGalleryRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyGalleryResponse;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostImageRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyGalleryService 테스트")
class JourneyGalleryServiceTest {

    private static final String IMAGE_URL = "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com/journeys/posts/img.png";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 10, 0);

    @Mock
    private JourneyMemberRepository journeyMemberRepository;
    @Mock
    private JourneyPostImageRepository journeyPostImageRepository;

    private JourneyGalleryService journeyGalleryService;

    @BeforeEach
    void setUp() {
        journeyGalleryService = new JourneyGalleryService(journeyMemberRepository, journeyPostImageRepository);
    }

    @Nested
    @DisplayName("갤러리 조회")
    class RetrieveGallery {

        @Test
        @DisplayName("active member가 아니면 접근이 거부된다")
        void nonMemberCannotAccessGallery() {
            Long journeyId = 1L;
            Long userId = 10L;

            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyGalleryService.retrieveGallery(journeyId, userId, new JourneyGalleryRequest(null, null, null)))
                    .isInstanceOf(JourneyAccessDeniedException.class);
        }

        @Test
        @DisplayName("이미지가 없으면 빈 목록을 반환한다")
        void returnsEmptyListWhenNoImages() {
            Long journeyId = 1L;
            Long userId = 10L;
            JourneyGalleryRequest request = new JourneyGalleryRequest(null, null, null);

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(null), eq(null), any()))
                    .thenReturn(List.of());

            JourneyGalleryResponse response = journeyGalleryService.retrieveGallery(journeyId, userId, request);

            assertThat(response.images()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("첫 페이지 조회 시 커서 없이 요청한다")
        void firstPageRequestHasNoCursor() {
            Long journeyId = 1L;
            Long userId = 10L;
            JourneyGalleryRequest request = new JourneyGalleryRequest(null, null, 10);

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(null), eq(null), any()))
                    .thenReturn(List.of());

            journeyGalleryService.retrieveGallery(journeyId, userId, request);

            verify(journeyPostImageRepository).findGalleryImages(journeyId, null, null, PageRequest.of(0, 11));
        }

        @Test
        @DisplayName("이미지가 있으면 올바른 응답을 반환한다")
        void returnsImagesCorrectly() {
            Long journeyId = 1L;
            Long userId = 10L;
            JourneyGalleryRequest request = new JourneyGalleryRequest(null, null, 20);

            JourneyPost post101 = createJourneyPost(101L, NOW.minusDays(1));
            JourneyPost post98 = createJourneyPost(98L, NOW.minusDays(2));
            JourneyPostImage img1 = JourneyPostImage.of(post101, IMAGE_URL + "?1", 0);
            JourneyPostImage img2 = JourneyPostImage.of(post101, IMAGE_URL + "?2", 1);
            JourneyPostImage img3 = JourneyPostImage.of(post98, IMAGE_URL + "?3", 0);

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(null), eq(null), any()))
                    .thenReturn(List.of(img1, img2, img3));

            JourneyGalleryResponse response = journeyGalleryService.retrieveGallery(journeyId, userId, request);

            assertThat(response.journeyId()).isEqualTo(journeyId);
            assertThat(response.images()).hasSize(3);
            assertThat(response.images().get(0).journeyPostId()).isEqualTo(101L);
            assertThat(response.images().get(0).sortOrder()).isEqualTo(0);
            assertThat(response.images().get(1).journeyPostId()).isEqualTo(101L);
            assertThat(response.images().get(1).sortOrder()).isEqualTo(1);
            assertThat(response.images().get(2).journeyPostId()).isEqualTo(98L);
            assertThat(response.images().get(2).sortOrder()).isEqualTo(0);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("다음 페이지가 있으면 hasNext=true와 nextCursor를 반환한다")
        void returnsNextCursorWhenHasNext() {
            Long journeyId = 1L;
            Long userId = 10L;
            int size = 2;
            JourneyGalleryRequest request = new JourneyGalleryRequest(null, null, size);

            JourneyPost post101 = createJourneyPost(101L, NOW);
            JourneyPost post98 = createJourneyPost(98L, NOW.minusDays(1));
            JourneyPostImage img1 = JourneyPostImage.of(post101, IMAGE_URL + "?1", 0);
            JourneyPostImage img2 = JourneyPostImage.of(post101, IMAGE_URL + "?2", 1);
            JourneyPostImage img3 = JourneyPostImage.of(post98, IMAGE_URL + "?3", 0); // look-ahead

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(null), eq(null), any()))
                    .thenReturn(List.of(img1, img2, img3));

            JourneyGalleryResponse response = journeyGalleryService.retrieveGallery(journeyId, userId, request);

            assertThat(response.images()).hasSize(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextCursor().journeyPostId()).isEqualTo(101L);
            assertThat(response.nextCursor().sortOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("복합 커서로 다음 페이지를 조회한다")
        void retrievesNextPageWithCompositeCursor() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long cursorPostId = 101L;
            Integer cursorSortOrder = 1;
            JourneyGalleryRequest request = new JourneyGalleryRequest(cursorPostId, cursorSortOrder, 20);

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(cursorPostId), eq(cursorSortOrder), any()))
                    .thenReturn(List.of());

            journeyGalleryService.retrieveGallery(journeyId, userId, request);

            verify(journeyPostImageRepository).findGalleryImages(journeyId, cursorPostId, cursorSortOrder, PageRequest.of(0, 21));
        }

        @Test
        @DisplayName("size가 50을 초과하면 50으로 클램프된다")
        void sizeIsClamped() {
            Long journeyId = 1L;
            Long userId = 10L;
            JourneyGalleryRequest request = new JourneyGalleryRequest(null, null, 100);

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(null), eq(null), any()))
                    .thenReturn(List.of());

            journeyGalleryService.retrieveGallery(journeyId, userId, request);

            verify(journeyPostImageRepository).findGalleryImages(journeyId, null, null, PageRequest.of(0, 51));
        }

        @Test
        @DisplayName("size가 null이면 기본값 20으로 조회한다")
        void defaultSizeIsApplied() {
            Long journeyId = 1L;
            Long userId = 10L;
            JourneyGalleryRequest request = new JourneyGalleryRequest(null, null, null);

            givenActiveMember(journeyId, userId);
            when(journeyPostImageRepository.findGalleryImages(eq(journeyId), eq(null), eq(null), any()))
                    .thenReturn(List.of());

            journeyGalleryService.retrieveGallery(journeyId, userId, request);

            verify(journeyPostImageRepository).findGalleryImages(journeyId, null, null, PageRequest.of(0, 21));
        }
    }

    private void givenActiveMember(Long journeyId, Long userId) {
        when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                .thenReturn(true);
    }

    private JourneyPost createJourneyPost(Long postId, LocalDateTime createdAt) {
        User author = User.builder()
                .email("author@example.com")
                .name("author")
                .oauthId("oauth-" + postId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(author, "id", postId + 100);

        JourneyPost post = JourneyPost.create(null, author, "테스트 게시글");
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        return post;
    }
}
