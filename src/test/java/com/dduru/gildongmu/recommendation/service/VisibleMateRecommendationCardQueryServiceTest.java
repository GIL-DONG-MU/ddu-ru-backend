package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.common.config.QueryDslConfig;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.recommendation.domain.MateRecommendation;
import com.dduru.gildongmu.recommendation.domain.MateRecommendationBatch;
import com.dduru.gildongmu.recommendation.domain.MateRecommendationPass;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.MateRecommendationCardQueryResult;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationBatchRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationCardQueryRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationPassRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationRepository;
import com.dduru.gildongmu.recommendation.support.AvailableDateRange;
import com.dduru.gildongmu.recommendation.support.RecommendationAvailableDateMatcher;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import com.dduru.gildongmu.report.domain.Report;
import com.dduru.gildongmu.report.domain.enums.ReportReason;
import com.dduru.gildongmu.report.repository.ReportRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, MateRecommendationCardQueryRepository.class})
@DisplayName("노출 가능한 메이트 추천 카드 조회 테스트")
class VisibleMateRecommendationCardQueryServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private DestinationRepository destinationRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private ParticipationRepository participationRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private MateRecommendationBatchRepository batchRepository;
    @Autowired private MateRecommendationRepository recommendationRepository;
    @Autowired private MateRecommendationPassRepository passRepository;
    @Autowired private MateRecommendationCardQueryRepository cardQueryRepository;

    private VisibleMateRecommendationCardQueryService queryService;
    private User applicant;
    private Destination jeju;
    private Destination busan;
    private int sequence;

    @BeforeEach
    void setUp() {
        queryService = new VisibleMateRecommendationCardQueryService(
                cardQueryRepository,
                new RecommendationAvailableDateMatcher()
        );
        applicant = user("applicant", Gender.F, LocalDate.of(1999, 7, 15));
        jeju = destination("KR", "대한민국", "제주");
        busan = destination("KR", "대한민국", "부산");
    }

    @Test
    @DisplayName("현재 신청 가능한 추천은 저장 순위와 스냅샷 점수로 조회한다")
    void returnsCurrentlyVisibleCard() {
        MateRecommendationBatch batch = batch();
        Post post = post(user("host", Gender.M, LocalDate.of(1995, 1, 1)), jeju,
                TODAY.plusDays(5), TODAY.plusDays(7), Gender.U, CompanionType.FULL, 4);
        recommendation(batch, post, 1, 88);
        batch.complete();

        List<MateRecommendationCardQueryResult> result = queryService.findVisibleCards(
                batch.getId(), applicant.getId(), context(Set.of(), Set.of(), List.of())
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).postId()).isEqualTo(post.getId());
        assertThat(result.get(0).matchPercentage()).isEqualTo(88);
        assertThat(result.get(0).recommendationRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("추천 후 모집 종료, 정원 마감, 삭제된 게시글은 숨긴다")
    void hidesClosedFullAndDeletedPosts() {
        MateRecommendationBatch batch = batch();
        Post closed = post(host("closed"), jeju, TODAY.plusDays(2), TODAY.plusDays(4), Gender.U, CompanionType.FULL, 4);
        closed.changeStatus(PostStatus.CLOSED);
        Post full = post(host("full"), jeju, TODAY.plusDays(3), TODAY.plusDays(5), Gender.U, CompanionType.FULL, 1);
        Post deleted = post(host("deleted"), jeju, TODAY.plusDays(4), TODAY.plusDays(6), Gender.U, CompanionType.FULL, 4);
        deleted.softDelete(deleted.getUser().getId(), TODAY.atStartOfDay());
        recommendation(batch, closed, 1, 90);
        recommendation(batch, full, 2, 89);
        recommendation(batch, deleted, 3, 88);
        batch.complete();

        List<MateRecommendationCardQueryResult> result = queryService.findVisibleCards(
                batch.getId(), applicant.getId(), context(Set.of(), Set.of(), List.of())
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("추천 후 여행이 종료된 게시글은 숨긴다")
    void hidesEndedPost() {
        MateRecommendationBatch batch = batch();
        Post ended = post(host("ended"), jeju, TODAY.minusDays(3), TODAY.minusDays(1), Gender.U, CompanionType.FULL, 4);
        recommendation(batch, ended, 1, 90);
        batch.complete();

        assertThat(queryService.findVisibleCards(
                batch.getId(), applicant.getId(), context(Set.of(), Set.of(), List.of())
        )).isEmpty();
    }

    @Test
    @DisplayName("추천 후 신청, 패스, 신고한 게시글은 숨긴다")
    void hidesInteractedPosts() {
        MateRecommendationBatch batch = batch();
        Post applied = post(host("applied"), jeju, TODAY.plusDays(2), TODAY.plusDays(4), Gender.U, CompanionType.FULL, 4);
        Post passed = post(host("passed"), jeju, TODAY.plusDays(3), TODAY.plusDays(5), Gender.U, CompanionType.FULL, 4);
        Post reported = post(host("reported"), jeju, TODAY.plusDays(4), TODAY.plusDays(6), Gender.U, CompanionType.FULL, 4);
        recommendation(batch, applied, 1, 90);
        recommendation(batch, passed, 2, 89);
        recommendation(batch, reported, 3, 88);
        batch.complete();
        participationRepository.save(Participation.createParticipation(applied, applicant, "신청 메시지입니다."));
        passRepository.save(MateRecommendationPass.of(applicant, passed));
        reportRepository.save(Report.createReport(applicant, reported, ReportReason.OTHER, "신고 내용"));

        List<MateRecommendationCardQueryResult> result = queryService.findVisibleCards(
                batch.getId(), applicant.getId(), context(Set.of(), Set.of(), List.of())
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("현재 여행지, 날짜, 성별 조건과 달라진 추천은 숨긴다")
    void hidesPostsThatNoLongerMatchCurrentFilters() {
        MateRecommendationBatch batch = batch();
        Post wrongDestination = post(host("destination"), busan,
                TODAY.plusDays(5), TODAY.plusDays(7), Gender.U, CompanionType.FULL, 4);
        Post wrongDate = post(host("date"), jeju,
                TODAY.plusDays(20), TODAY.plusDays(22), Gender.U, CompanionType.FULL, 4);
        Post wrongGender = post(host("gender"), jeju,
                TODAY.plusDays(5), TODAY.plusDays(7), Gender.M, CompanionType.FULL, 4);
        recommendation(batch, wrongDestination, 1, 90);
        recommendation(batch, wrongDate, 2, 89);
        recommendation(batch, wrongGender, 3, 88);
        batch.complete();

        RecommendationApplicantContext context = context(
                Set.of(),
                Set.of(jeju.getId()),
                List.of(new AvailableDateRange(TODAY.plusDays(5), TODAY.plusDays(7)))
        );
        List<MateRecommendationCardQueryResult> result = queryService.findVisibleCards(
                batch.getId(), applicant.getId(), context
        );

        assertThat(result).isEmpty();
    }

    private RecommendationApplicantContext context(
            Set<String> countryCodes,
            Set<Long> destinationIds,
            List<AvailableDateRange> dateRanges
    ) {
        return new RecommendationApplicantContext(
                TODAY,
                Gender.F,
                27,
                new DestinationPreferenceFilter(countryCodes, destinationIds),
                dateRanges,
                new TravelTendencyScores(5, 5, 5, 5)
        );
    }

    private MateRecommendationBatch batch() {
        return batchRepository.save(MateRecommendationBatch.create(applicant, TODAY));
    }

    private void recommendation(MateRecommendationBatch batch, Post post, int rank, int percentage) {
        recommendationRepository.save(MateRecommendation.create(batch, post, rank, percentage, "[]", "[]"));
    }

    private User host(String suffix) {
        return user(suffix, Gender.M, LocalDate.of(1995, 1, 1));
    }

    private User user(String suffix, Gender gender, LocalDate birthday) {
        sequence++;
        User user = userRepository.save(User.builder()
                .email(suffix + sequence + "@example.com")
                .name("사용자" + sequence)
                .oauthId("oauth-" + suffix + sequence)
                .oauthType(OauthType.KAKAO)
                .build());
        Profile profile = new Profile(user);
        profile.setupInitialProfile(gender, null, birthday);
        profile.updateNickname("닉네임" + sequence);
        profileRepository.save(profile);
        return user;
    }

    private Destination destination(String countryCode, String countryName, String city) {
        return destinationRepository.save(Destination.builder()
                .countryCode(countryCode)
                .countryName(countryName)
                .city(city)
                .image("https://example.com/" + city + ".jpg")
                .build());
    }

    private Post post(
            User host,
            Destination destination,
            LocalDate startDate,
            LocalDate endDate,
            Gender preferredGender,
            CompanionType companionType,
            int capacity
    ) {
        sequence++;
        return postRepository.save(Post.createPost(
                host,
                destination,
                "현재 추천 조건 테스트 " + sequence,
                "현재 추천 조건을 다시 검증하기 위한 충분한 길이의 본문입니다.",
                startDate,
                endDate,
                capacity,
                endDate.minusDays(1),
                preferredGender,
                true,
                null,
                null,
                null,
                "[\"힐링\"]",
                companionType
        ));
    }
}
