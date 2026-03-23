package com.dduru.gildongmu.report.service;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.report.domain.Report;
import com.dduru.gildongmu.report.domain.enums.ReportReason;
import com.dduru.gildongmu.report.dto.request.ReportCreateRequest;
import com.dduru.gildongmu.report.dto.response.ReportCreateResponse;
import com.dduru.gildongmu.report.exception.DuplicatePostReportException;
import com.dduru.gildongmu.report.exception.SelfPostReportNotAllowedException;
import com.dduru.gildongmu.report.repository.ReportRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 테스트")
class ReportServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @DisplayName("신고 생성 성공 시 신고 ID와 상태를 반환한다")
    @Test
    void create_validRequest_returnsReportCreateResponse() {
        Long postId = 10L;
        Long userId = 1L;

        User reporter = createUser(userId, "reporter@dduru.com", "신고자");
        User postOwner = createUser(2L, "owner@dduru.com", "작성자");
        Post post = createPost(postId, postOwner);
        ReportCreateRequest request = new ReportCreateRequest(ReportReason.SPAM_AD, "광고 게시물입니다.");

        when(userRepository.getByIdOrThrow(userId)).thenReturn(reporter);
        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(reportRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            ReflectionTestUtils.setField(report, "id", 100L);
            return report;
        });

        ReportCreateResponse response = reportService.create(postId, userId, request);

        assertThat(response.reportId()).isEqualTo(100L);
        assertThat(response.status()).isNotNull();
        verify(reportRepository).save(any(Report.class));
    }

    @DisplayName("본인 게시글 신고 시 예외가 발생한다")
    @Test
    void create_selfPost_throwsSelfPostReportNotAllowedException() {
        Long postId = 10L;
        Long userId = 1L;

        User user = createUser(userId, "same@dduru.com", "본인");
        Post post = createPost(postId, user);
        ReportCreateRequest request = new ReportCreateRequest(ReportReason.OTHER, "본인 글 신고 시도");

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);

        assertThatThrownBy(() -> reportService.create(postId, userId, request))
                .isInstanceOf(SelfPostReportNotAllowedException.class);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @DisplayName("이미 신고한 게시글이면 예외가 발생한다")
    @Test
    void create_duplicateReport_throwsDuplicatePostReportException() {
        Long postId = 10L;
        Long userId = 1L;

        User reporter = createUser(userId, "reporter@dduru.com", "신고자");
        User postOwner = createUser(2L, "owner@dduru.com", "작성자");
        Post post = createPost(postId, postOwner);
        ReportCreateRequest request = new ReportCreateRequest(ReportReason.DUPLICATE, "이미 신고한 게시물");

        when(userRepository.getByIdOrThrow(userId)).thenReturn(reporter);
        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(reportRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(true);

        assertThatThrownBy(() -> reportService.create(postId, userId, request))
                .isInstanceOf(DuplicatePostReportException.class);

        verify(reportRepository, never()).save(any(Report.class));
    }

    private User createUser(Long id, String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .oauthId("kakao-" + id)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post createPost(Long postId, User owner) {
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("신고 대상 게시글 제목")
                .content("신고 대상 게시글 본문은 충분히 긴 내용으로 작성합니다.")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .recruitCapacity(4)
                .recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M)
                .preferredAges(List.of(AgeRange.AGE_20s))
                .photoUrls("[]")
                .tags("[]")
                .recruitType(RecruitType.PRIVATE)
                .recruitMethod(RecruitMethod.ALWAYS)
                .companionType(null)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
