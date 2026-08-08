package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.common.config.QueryDslConfig;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.recommendation.domain.MateRecommendation;
import com.dduru.gildongmu.recommendation.domain.MateRecommendationBatch;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.RecommendationReason;
import com.dduru.gildongmu.recommendation.dto.result.ScoredPostRecommendation;
import com.dduru.gildongmu.recommendation.exception.MateRecommendationNotFoundException;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationBatchRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationPassRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationRepository;
import com.dduru.gildongmu.recommendation.support.RecommendationReasonJsonConverter;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(QueryDslConfig.class)
@DisplayName("추천 묶음 저장과 패스 테스트")
class MateRecommendationPersistenceServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Autowired private UserRepository userRepository;
    @Autowired private DestinationRepository destinationRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private MateRecommendationBatchRepository batchRepository;
    @Autowired private MateRecommendationRepository recommendationRepository;
    @Autowired private MateRecommendationPassRepository passRepository;

    private RecommendationBatchCompletionService completionService;
    private MateRecommendationPassService passService;
    private RecommendationReasonJsonConverter reasonJsonConverter;
    private User applicant;
    private Destination destination;
    private int sequence;

    @BeforeEach
    void setUp() {
        reasonJsonConverter = new RecommendationReasonJsonConverter(new ObjectMapper());
        completionService = new RecommendationBatchCompletionService(
                batchRepository,
                recommendationRepository,
                postRepository,
                reasonJsonConverter
        );
        passService = new MateRecommendationPassService(recommendationRepository, passRepository, userRepository);
        applicant = user("applicant");
        destination = destinationRepository.save(Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .image("https://example.com/jeju.jpg")
                .build());
    }

    @Test
    @DisplayName("후보를 순위와 추천 이유 JSON으로 저장하고 배치를 COMPLETED로 전환한다")
    void storesCompletedRecommendations() {
        MateRecommendationBatch batch = batchRepository.save(MateRecommendationBatch.create(applicant, TODAY));
        Post first = post(user("host-1"), TODAY.plusDays(1));
        Post second = post(user("host-2"), TODAY.plusDays(3));
        RecommendationReason reason = new RecommendationReason("RHYTHM_MATCH", "여행 리듬이 잘 맞아요");

        completionService.complete(batch.getId(), PostRecommendationResult.ready(List.of(
                scored(first, 91, List.of(reason)),
                scored(second, 83, List.of())
        )));

        List<MateRecommendation> saved = recommendationRepository
                .findAllByBatch_IdOrderByRecommendationRankAsc(batch.getId());
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(MateRecommendation::getRecommendationRank).containsExactly(1, 2);
        assertThat(saved).extracting(MateRecommendation::getMatchPercentage).containsExactly(91, 83);
        assertThat(reasonJsonConverter.fromJson(saved.get(0).getMatchReasons())).containsExactly(reason);
        assertThat(batchRepository.findById(batch.getId()).orElseThrow().getStatus())
                .isEqualTo(MateRecommendationBatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("후보가 없으면 추천 row 없이 배치를 EMPTY로 전환한다")
    void storesEmptyBatch() {
        MateRecommendationBatch batch = batchRepository.save(MateRecommendationBatch.create(applicant, TODAY));

        completionService.complete(batch.getId(), PostRecommendationResult.noCandidates());

        assertThat(recommendationRepository.findAllByBatch_IdOrderByRecommendationRankAsc(batch.getId())).isEmpty();
        assertThat(batchRepository.findById(batch.getId()).orElseThrow().getStatus())
                .isEqualTo(MateRecommendationBatchStatus.EMPTY);
    }

    @Test
    @DisplayName("본인 추천 패스는 재호출해도 이력을 한 건만 저장한다")
    void passesIdempotently() {
        MateRecommendation recommendation = recommendation();

        passService.pass(applicant.getId(), recommendation.getId());
        passService.pass(applicant.getId(), recommendation.getId());

        assertThat(passRepository.findAll()).hasSize(1);
        assertThat(passRepository.existsByUser_IdAndPost_Id(applicant.getId(), recommendation.getPost().getId())).isTrue();
    }

    @Test
    @DisplayName("다른 사용자의 추천은 패스할 수 없다")
    void rejectsAnotherUsersRecommendation() {
        MateRecommendation recommendation = recommendation();
        User other = user("other");

        assertThatThrownBy(() -> passService.pass(other.getId(), recommendation.getId()))
                .isInstanceOf(MateRecommendationNotFoundException.class);
    }

    private MateRecommendation recommendation() {
        MateRecommendationBatch batch = batchRepository.save(MateRecommendationBatch.create(applicant, TODAY));
        Post post = post(user("host"), TODAY.plusDays(1));
        MateRecommendation recommendation = MateRecommendation.create(batch, post, 1, 90, "[]", "[]");
        batch.complete();
        return recommendationRepository.save(recommendation);
    }

    private ScoredPostRecommendation scored(Post post, int percentage, List<RecommendationReason> reasons) {
        return new ScoredPostRecommendation(
                post.getId(),
                post.getStartDate(),
                post.getEndDate(),
                percentage,
                reasons,
                List.of()
        );
    }

    private Post post(User host, LocalDate startDate) {
        sequence++;
        return postRepository.save(Post.createPost(
                host,
                destination,
                "추천 저장 테스트 " + sequence,
                "추천 저장 테스트를 위한 충분한 길이의 게시글 본문입니다.",
                startDate,
                startDate.plusDays(2),
                4,
                startDate.minusDays(1),
                Gender.U,
                true,
                null,
                null,
                null,
                "[\"힐링\"]",
                CompanionType.FULL
        ));
    }

    private User user(String suffix) {
        sequence++;
        return userRepository.save(User.builder()
                .email(suffix + sequence + "@example.com")
                .name("사용자" + sequence)
                .oauthId("oauth-" + suffix + sequence)
                .oauthType(OauthType.KAKAO)
                .build());
    }
}
