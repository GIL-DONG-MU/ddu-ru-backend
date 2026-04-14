package com.dduru.gildongmu.participation.domain;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Participation 상태 전이 테스트")
class ParticipationTest {

    @Test
    @DisplayName("PENDING 상태에서 연락하면 CONTACTING으로 바뀌고 contactedAt이 기록된다")
    void contact_pending_changesStatusAndTimestamp() {
        Participation participation = createParticipation();

        participation.contact();

        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.CONTACTING);
        assertThat(participation.getContactedAt()).isNotNull();
    }

    @Test
    @DisplayName("PENDING 상태에서 승인하면 APPROVED로 바뀌고 approvedAt이 기록된다")
    void approve_pending_changesStatusAndTimestamp() {
        Participation participation = createParticipation();

        participation.approve();

        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
        assertThat(participation.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("CONTACTING 상태에서도 승인할 수 있다")
    void approve_contacting_changesStatusAndTimestamp() {
        Participation participation = createParticipation();
        participation.contact();

        participation.approve();

        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
        assertThat(participation.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("PENDING 상태에서 거절하면 REJECTED로 바뀌고 rejectedAt이 기록된다")
    void reject_pending_changesStatusAndTimestamp() {
        Participation participation = createParticipation();

        participation.reject();

        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.REJECTED);
        assertThat(participation.getRejectedAt()).isNotNull();
    }

    @Test
    @DisplayName("CONTACTING 상태에서도 거절할 수 있다")
    void reject_contacting_changesStatusAndTimestamp() {
        Participation participation = createParticipation();
        participation.contact();

        participation.reject();

        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.REJECTED);
        assertThat(participation.getRejectedAt()).isNotNull();
    }

    @Test
    @DisplayName("APPROVED 상태에서 다시 승인하면 approvalNotAllowed 예외가 발생한다")
    void approve_approved_throwsException() {
        Participation participation = createParticipation();
        participation.approve();

        assertThatThrownBy(participation::approve)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARTICIPATION_APPROVAL_NOT_ALLOWED);
    }

    @Test
    @DisplayName("REJECTED 상태에서 연락하면 contactNotAllowed 예외가 발생한다")
    void contact_rejected_throwsException() {
        Participation participation = createParticipation();
        participation.reject();

        assertThatThrownBy(participation::contact)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARTICIPATION_CONTACT_NOT_ALLOWED);
    }

    @Test
    @DisplayName("APPROVED 상태에서 거절하면 rejectionNotAllowed 예외가 발생한다")
    void reject_approved_throwsException() {
        Participation participation = createParticipation();
        participation.approve();

        assertThatThrownBy(participation::reject)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARTICIPATION_REJECTION_NOT_ALLOWED);
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
        Post post = Post.builder()
                .user(author)
                .recruitCapacity(3)
                .build();
        return Participation.createParticipation(post, participant, "hello");
    }
}
