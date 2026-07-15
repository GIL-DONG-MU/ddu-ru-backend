package com.dduru.gildongmu.participation.domain;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Participation 상태 전이 테스트")
class ParticipationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 11, 12, 0);

    @Nested
    @DisplayName("연락")
    class Contact {

        @Test
        @DisplayName("PENDING 상태에서 연락하면 CONTACTING으로 바뀌고 contactedAt이 기록된다")
        void pendingChangesStatusAndTimestamp() {
            Participation participation = createParticipation();

            participation.contact(NOW);

            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.CONTACTING);
            assertThat(participation.getContactedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("REJECTED 상태에서 연락하면 예외가 발생한다")
        void rejectedThrowsException() {
            Participation participation = createParticipation();
            participation.reject(NOW);

            assertThatThrownBy(() -> participation.contact(NOW))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PARTICIPATION_CONTACT_NOT_ALLOWED);
        }
    }

    @Nested
    @DisplayName("승인")
    class Approve {

        @Test
        @DisplayName("PENDING 상태에서 승인하면 APPROVED로 바뀌고 approvedAt이 기록된다")
        void pendingChangesStatusAndTimestamp() {
            Participation participation = createParticipation();

            participation.approve(NOW);

            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
            assertThat(participation.getApprovedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("CONTACTING 상태에서도 승인할 수 있다")
        void contactingChangesStatusAndTimestamp() {
            Participation participation = createParticipation();
            participation.contact(NOW);

            participation.approve(NOW);

            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
            assertThat(participation.getApprovedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("APPROVED 상태에서 다시 승인하면 예외가 발생한다")
        void approvedThrowsException() {
            Participation participation = createParticipation();
            participation.approve(NOW);

            assertThatThrownBy(() -> participation.approve(NOW))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PARTICIPATION_APPROVAL_NOT_ALLOWED);
        }
    }

    @Nested
    @DisplayName("거절")
    class Reject {

        @Test
        @DisplayName("PENDING 상태에서 거절하면 REJECTED로 바뀌고 rejectedAt이 기록된다")
        void pendingChangesStatusAndTimestamp() {
            Participation participation = createParticipation();

            participation.reject(NOW);

            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.REJECTED);
            assertThat(participation.getRejectedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("CONTACTING 상태에서도 거절할 수 있다")
        void contactingChangesStatusAndTimestamp() {
            Participation participation = createParticipation();
            participation.contact(NOW);

            participation.reject(NOW);

            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.REJECTED);
            assertThat(participation.getRejectedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("APPROVED 상태에서 거절하면 예외가 발생한다")
        void approvedThrowsException() {
            Participation participation = createParticipation();
            participation.approve(NOW);

            assertThatThrownBy(() -> participation.reject(NOW))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PARTICIPATION_REJECTION_NOT_ALLOWED);
        }
    }

    private Participation createParticipation() {
        User author = User.builder()
                .email("author@example.com")
                .name("author")
                .oauthId("author-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        User participant = User.builder()
                .email("participant@example.com")
                .name("participant")
                .oauthId("participant-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        Post post = Post.createPost(
                author,
                Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build(),
                "참여 테스트 게시글",
                "참여 테스트 본문은 충분히 긴 내용입니다.",
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
        return Participation.createParticipation(post, participant, "hello");
    }
}
