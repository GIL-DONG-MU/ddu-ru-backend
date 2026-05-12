package com.dduru.gildongmu.post.domain;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
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
        User author = User.builder()
                .email("author@example.com")
                .name("author")
                .oauthId("author-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("서울")
                .build();
        return Post.createPost(
                author,
                destination,
                "테스트 게시글 제목",
                "테스트 게시글 본문은 충분히 긴 내용입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                3,
                LocalDate.now().plusDays(1),
                Gender.U,
                true,
                null,
                null,
                null,
                "[]",
                CompanionType.FULL
        );
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
