package com.dduru.gildongmu.admin.report.service;

import com.dduru.gildongmu.admin.report.dto.request.AdminReportUpdateRequest;
import com.dduru.gildongmu.admin.report.dto.response.AdminReportListResponse;
import com.dduru.gildongmu.admin.report.dto.response.AdminReportResponse;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.report.domain.Report;
import com.dduru.gildongmu.report.domain.enums.ReportReason;
import com.dduru.gildongmu.report.domain.enums.ReportStatus;
import com.dduru.gildongmu.report.exception.ReportNotFoundException;
import com.dduru.gildongmu.report.repository.ReportRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.domain.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReportService 테스트")
class AdminReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminReportService adminReportService;

    @DisplayName("신고 목록 조회 시 상태 필터를 레포지토리에 전달한다")
    @Test
    void findAll_withStatusFilter_passesFilterToRepository() {
        ReportStatus status = ReportStatus.IN_REVIEW;
        Pageable pageable = PageRequest.of(0, 20);

        Report report = createReport(1L);
        Page<Report> page = new PageImpl<>(List.of(report), pageable, 1);
        when(reportRepository.findAllByStatusOptional(status, pageable)).thenReturn(page);

        AdminReportListResponse response = adminReportService.findAll(status, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).status()).isEqualTo(ReportStatus.RECEIVED);
        verify(reportRepository).findAllByStatusOptional(status, pageable);
    }

    @DisplayName("신고 처리 시 reviewer/reviewedAt/status/reviewNote가 갱신된다")
    @Test
    void update_updatesReviewerReviewedAtStatusAndReviewNote() {
        Long reportId = 11L;
        Long reviewerId = 99L;
        Report report = createReport(reportId);
        User reviewer = createUser(reviewerId, "admin@dduru.com", "관리자", Role.ADMIN);
        AdminReportUpdateRequest request = new AdminReportUpdateRequest(ReportStatus.RESOLVED, "검토 및 조치 완료");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.getByIdOrThrow(reviewerId)).thenReturn(reviewer);

        AdminReportResponse response = adminReportService.update(reportId, reviewerId, request);

        assertThat(response.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(response.reviewerId()).isEqualTo(reviewerId);
        assertThat(response.reviewerName()).isEqualTo("관리자");
        assertThat(response.reviewedAt()).isNotNull();
        assertThat(response.reviewNote()).isEqualTo("검토 및 조치 완료");
    }

    @DisplayName("존재하지 않는 신고를 처리하면 예외가 발생한다")
    @Test
    void update_whenReportNotFound_throwsReportNotFoundException() {
        Long reportId = 999L;
        Long reviewerId = 1L;
        AdminReportUpdateRequest request = new AdminReportUpdateRequest(ReportStatus.REJECTED, "근거 부족");

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminReportService.update(reportId, reviewerId, request))
                .isInstanceOf(ReportNotFoundException.class);
    }

    private Report createReport(Long reportId) {
        User reporter = createUser(2L, "reporter@dduru.com", "신고자", Role.USER);
        User owner = createUser(3L, "owner@dduru.com", "작성자", Role.USER);
        Post post = createPost(21L, owner);
        Report report = Report.createReport(reporter, post, ReportReason.SPAM_AD, "스팸 신고");
        ReflectionTestUtils.setField(report, "id", reportId);
        return report;
    }

    private Post createPost(Long postId, User owner) {
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("신고 대상 게시글")
                .content("신고 대상 게시글 본문은 충분한 길이로 작성합니다.")
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

    private User createUser(Long id, String email, String name, Role role) {
        User user = User.builder()
                .email(email)
                .name(name)
                .oauthId("kakao-" + id)
                .oauthType(OauthType.KAKAO)
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
