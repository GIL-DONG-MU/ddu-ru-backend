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
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationBatchRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationPassRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationRepository;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({
        QueryDslConfig.class,
        RecommendationBatchClaimService.class,
        DailyMateRecommendationService.class,
        MateRecommendationPassService.class
})
@DisplayName("추천 동시성 통합 테스트")
class MateRecommendationConcurrencyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Autowired private UserRepository userRepository;
    @Autowired private DestinationRepository destinationRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private MateRecommendationBatchRepository batchRepository;
    @Autowired private MateRecommendationRepository recommendationRepository;
    @Autowired private MateRecommendationPassRepository passRepository;
    @Autowired private DailyMateRecommendationService dailyService;
    @Autowired private MateRecommendationPassService passService;

    @MockitoBean private RecommendationApplicantContextResolver contextResolver;
    @MockitoBean private PostRecommendationSelectionService selectionService;
    @MockitoBean private RecommendationBatchCompletionService completionService;
    @MockitoBean private RecommendationBatchFailureService failureService;

    private User applicant;
    private Destination destination;

    @BeforeEach
    void setUp() {
        applicant = userRepository.save(User.builder()
                .email("concurrency@example.com")
                .name("동시성테스트")
                .oauthId("concurrency-oauth")
                .oauthType(OauthType.KAKAO)
                .build());
        destination = destinationRepository.save(Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .image("https://example.com/jeju.jpg")
                .build());
    }

    @AfterEach
    void tearDown() {
        passRepository.deleteAll();
        recommendationRepository.deleteAll();
        batchRepository.deleteAll();
        postRepository.deleteAll();
        destinationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 날짜의 병렬 홈 호출은 배치를 하나만 만들고 후보를 한 번만 계산한다")
    void createsOneBatchForConcurrentHomeRequests() throws Exception {
        RecommendationApplicantContext context = context();
        when(contextResolver.resolve(applicant.getId())).thenReturn(Optional.of(context));
        when(selectionService.selectRecommendations(applicant.getId(), context))
                .thenReturn(PostRecommendationResult.noCandidates());

        runConcurrently(
                () -> dailyService.getOrCreate(applicant.getId()),
                () -> dailyService.getOrCreate(applicant.getId())
        );

        assertThat(batchRepository.count()).isOne();
        verify(selectionService).selectRecommendations(applicant.getId(), context);
    }

    @Test
    @DisplayName("서로 다른 추천 ID로 같은 게시글을 동시에 패스해도 이력은 한 건만 저장한다")
    void passesSamePostIdempotentlyWithDifferentRecommendationIds() throws Exception {
        Post post = post();
        MateRecommendation first = recommendation(post, TODAY);
        MateRecommendation second = recommendation(post, TODAY.plusDays(1));

        runConcurrently(
                () -> {
                    passService.pass(applicant.getId(), first.getId());
                    return null;
                },
                () -> {
                    passService.pass(applicant.getId(), second.getId());
                    return null;
                }
        );

        assertThat(passRepository.findAll()).hasSize(1);
    }

    @SafeVarargs
    private <T> List<T> runConcurrently(java.util.concurrent.Callable<T>... tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch ready = new CountDownLatch(tasks.length);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = Arrays.stream(tasks)
                    .map(task -> executor.submit(() -> {
                        ready.countDown();
                        if (!start.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Concurrent test start timed out");
                        }
                        return task.call();
                    }))
                    .toList();
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            return futures.stream()
                    .map(this::getResult)
                    .toList();
        } finally {
            executor.shutdownNow();
        }
    }

    private <T> T getResult(Future<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("Concurrent execution failed", e);
        }
    }

    private RecommendationApplicantContext context() {
        return new RecommendationApplicantContext(
                TODAY,
                Gender.F,
                27,
                new DestinationPreferenceFilter(Set.of(), Set.of()),
                List.of(),
                new TravelTendencyScores(5, 5, 5, 5)
        );
    }

    private MateRecommendation recommendation(Post post, LocalDate recommendationDate) {
        MateRecommendationBatch batch = batchRepository.save(
                MateRecommendationBatch.create(applicant, recommendationDate)
        );
        batch.complete();
        batchRepository.save(batch);
        return recommendationRepository.save(
                MateRecommendation.create(batch, post, 1, 90, "[]", "[]")
        );
    }

    private Post post() {
        User host = userRepository.save(User.builder()
                .email("host@example.com")
                .name("호스트")
                .oauthId("host-oauth")
                .oauthType(OauthType.KAKAO)
                .build());
        return postRepository.save(Post.createPost(
                host,
                destination,
                "동시 패스 테스트 게시글",
                "동일 게시글의 병렬 패스 처리를 검증하기 위한 충분한 길이의 본문입니다.",
                TODAY.plusDays(3),
                TODAY.plusDays(5),
                4,
                TODAY.plusDays(2),
                Gender.U,
                true,
                null,
                null,
                null,
                "[]",
                CompanionType.FULL
        ));
    }
}
