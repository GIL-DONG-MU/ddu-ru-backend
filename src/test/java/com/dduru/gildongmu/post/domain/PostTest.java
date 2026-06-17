package com.dduru.gildongmu.post.domain;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.exception.InvalidPostContentException;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidPostTitleException;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Post 테스트")
class PostTest {

    @Nested
    @DisplayName("게시글 생성 검증")
    class CreatePost {

        @Test
        @DisplayName("제목이 4자 이하면 InvalidPostTitleException이 발생한다")
        void titleTooShortThrows() {
            assertThatThrownBy(() -> createPost("짧음", validContent()))
                    .isInstanceOf(InvalidPostTitleException.class);
        }

        @Test
        @DisplayName("제목이 40자를 초과하면 InvalidPostTitleException이 발생한다")
        void titleTooLongThrows() {
            assertThatThrownBy(() -> createPost("제".repeat(41), validContent()))
                    .isInstanceOf(InvalidPostTitleException.class);
        }

        @Test
        @DisplayName("제목이 5자이면 생성에 성공한다")
        void titleMinLengthSucceeds() {
            Post post = createPost("가나다라마", validContent());
            assertThat(post.getTitle()).isEqualTo("가나다라마");
        }

        @Test
        @DisplayName("제목이 40자이면 생성에 성공한다")
        void titleMaxLengthSucceeds() {
            String title = "가".repeat(40);
            Post post = createPost(title, validContent());
            assertThat(post.getTitle()).isEqualTo(title);
        }

        @Test
        @DisplayName("내용이 19자 이하면 InvalidPostContentException이 발생한다")
        void contentTooShortThrows() {
            assertThatThrownBy(() -> createPost(validTitle(), "짧은내용"))
                    .isInstanceOf(InvalidPostContentException.class);
        }

        @Test
        @DisplayName("내용이 1000자를 초과하면 InvalidPostContentException이 발생한다")
        void contentTooLongThrows() {
            assertThatThrownBy(() -> createPost(validTitle(), "내".repeat(1001)))
                    .isInstanceOf(InvalidPostContentException.class);
        }

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 InvalidPostDateException이 발생한다")
        void endBeforeStartThrows() {
            LocalDate start = LocalDate.now().plusDays(5);
            LocalDate end = LocalDate.now().plusDays(3);
            assertThatThrownBy(() -> Post.createPost(
                    createAuthor(), createDestination(),
                    validTitle(), validContent(),
                    start, end, 3, end.minusDays(1),
                    Gender.U, true, null, null, null, "[]", CompanionType.FULL
            )).isInstanceOf(InvalidPostDateException.class);
        }

        private Post createPost(String title, String content) {
            LocalDate start = LocalDate.now().plusDays(1);
            LocalDate end = LocalDate.now().plusDays(3);
            return Post.createPost(
                    createAuthor(), createDestination(),
                    title, content, start, end, 3, end.minusDays(1),
                    Gender.U, true, null, null, null, "[]", CompanionType.FULL
            );
        }

        private String validTitle() { return "테스트 게시글 제목"; }
        private String validContent() { return "테스트 게시글 본문은 충분히 긴 내용입니다."; }
    }

    @Nested
    @DisplayName("참여 신청 승인")
    class ApproveParticipation {

        @Test
        @DisplayName("PENDING 참여 신청을 승인하면 상태가 APPROVED로 바뀌고 모집 인원이 증가한다")
        void pendingChangesStatusAndRecruitCount() {
            Post post = createPost();
            Participation participation = createParticipation(post);

            post.approveParticipation(participation);

            assertThat(participation.isApproved()).isTrue();
            assertThat(post.getRecruitCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("CONTACTING 참여 신청도 승인할 수 있고 모집 인원이 증가한다")
        void contactingChangesStatusAndRecruitCount() {
            Post post = createPost();
            Participation participation = createParticipation(post);
            participation.contact();

            post.approveParticipation(participation);

            assertThat(participation.isApproved()).isTrue();
            assertThat(post.getRecruitCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("이미 APPROVED 상태인 참여 신청을 다시 승인하면 예외가 발생하고 모집 인원은 유지된다")
        void approvedThrowsAndKeepsRecruitCount() {
            Post post = createPost();
            Participation participation = createParticipation(post);
            post.approveParticipation(participation);

            assertThatThrownBy(() -> post.approveParticipation(participation))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PARTICIPATION_APPROVAL_NOT_ALLOWED);
            assertThat(post.getRecruitCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("REJECTED 상태인 참여 신청을 승인하면 예외가 발생하고 모집 인원은 증가하지 않는다")
        void rejectedThrowsAndKeepsRecruitCount() {
            Post post = createPost();
            Participation participation = createParticipation(post);
            participation.reject();

            assertThatThrownBy(() -> post.approveParticipation(participation))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PARTICIPATION_APPROVAL_NOT_ALLOWED);
            assertThat(post.getRecruitCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("승인된 참여자 제거")
    class RemoveApprovedParticipation {

        @Test
        @DisplayName("APPROVED 참여자를 현재 모집 인원에서 제외하면 모집 인원이 감소한다")
        void approvedChangesStatusAndRecruitCount() {
            Post post = createPost();
            Participation participation = createParticipation(post);
            post.approveParticipation(participation);

            post.decrementRecruitCountIfApproved(participation);

            assertThat(participation.isApproved()).isTrue();
            assertThat(post.getRecruitCount()).isEqualTo(1);
        }
    }

    private Post createPost() {
        return Post.createPost(
                createAuthor(), createDestination(),
                "테스트 게시글 제목",
                "테스트 게시글 본문은 충분히 긴 내용입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                3,
                LocalDate.now().plusDays(1),
                Gender.U, true, null, null, null, "[]", CompanionType.FULL
        );
    }

    private User createAuthor() {
        return User.builder()
                .email("author@example.com")
                .name("author")
                .oauthId("author-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
    }

    private Destination createDestination() {
        return Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("서울")
                .build();
    }

    private Participation createParticipation(Post post) {
        User participant = User.builder()
                .email("participant@example.com")
                .name("participant")
                .oauthId("participant-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        return Participation.createParticipation(post, participant, "hello");
    }
}
