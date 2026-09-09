package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.common.config.QueryDslConfig;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("JourneyRepository 테스트")
class JourneyRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 13);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 13, 12, 0);

    @Autowired
    private JourneyRepository journeyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private int sequence;

    @Test
    @DisplayName("회원이 참여 중인 삭제되지 않은 진행·예정 여정만 조회한다")
    void findCurrentAndUpcomingJourneysFiltersUnavailableJourneys() {
        User currentUser = persistUser("current");
        User otherUser = persistUser("other");
        Destination destination = persistDestination();

        Journey currentJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.minusDays(2),
                TODAY.plusDays(1),
                true,
                false
        );
        Journey upcomingJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.plusDays(2),
                TODAY.plusDays(4),
                true,
                false
        );
        persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.minusDays(5),
                TODAY.plusDays(1),
                false,
                false
        );
        persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.minusDays(4),
                TODAY.plusDays(1),
                true,
                true
        );
        persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.minusDays(5),
                TODAY.minusDays(1),
                true,
                false
        );
        persistJourney(
                currentUser,
                otherUser,
                destination,
                TODAY.minusDays(3),
                TODAY.plusDays(1),
                true,
                false
        );
        flushAndClear();

        List<Journey> result = journeyRepository.findCurrentAndUpcomingJourneys(
                currentUser.getId(),
                TODAY,
                PageRequest.of(0, 10)
        );

        assertThat(result)
                .extracting(Journey::getId)
                .containsExactly(currentJourney.getId(), upcomingJourney.getId());
    }

    @Test
    @DisplayName("진행·예정 여정을 시작일 오름차순, 여정 ID 오름차순으로 조회한다")
    void findCurrentAndUpcomingJourneysOrdersByStartDateAndJourneyId() {
        User currentUser = persistUser("current");
        Destination destination = persistDestination();

        Journey upcomingJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.plusDays(3),
                TODAY.plusDays(5),
                true,
                false
        );
        Journey sameStartFirstJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.plusDays(1),
                TODAY.plusDays(3),
                true,
                false
        );
        Journey sameStartSecondJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.plusDays(1),
                TODAY.plusDays(4),
                true,
                false
        );
        Journey currentJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.minusDays(1),
                TODAY.plusDays(1),
                true,
                false
        );
        flushAndClear();

        List<Journey> result = journeyRepository.findCurrentAndUpcomingJourneys(
                currentUser.getId(),
                TODAY,
                PageRequest.of(0, 10)
        );

        assertThat(result)
                .extracting(Journey::getId)
                .containsExactly(
                        currentJourney.getId(),
                        sameStartFirstJourney.getId(),
                        sameStartSecondJourney.getId(),
                        upcomingJourney.getId()
                );
    }

    @Test
    @DisplayName("진행·예정 여정 중 시작일이 가장 빠른 한 건만 조회한다")
    void findNearestCurrentOrUpcomingJourneyReturnsOnlyFirstJourney() {
        User currentUser = persistUser("current");
        Destination destination = persistDestination();

        Journey nearestJourney = persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.minusDays(1),
                TODAY.plusDays(1),
                true,
                false
        );
        persistJourney(
                currentUser,
                currentUser,
                destination,
                TODAY.plusDays(1),
                TODAY.plusDays(3),
                true,
                false
        );
        flushAndClear();

        assertThat(journeyRepository.findNearestCurrentOrUpcomingJourney(currentUser.getId(), TODAY))
                .hasValueSatisfying(journey -> assertThat(journey.getId()).isEqualTo(nearestJourney.getId()));
    }

    private User persistUser(String name) {
        sequence++;
        return entityManager.persist(User.builder()
                .email(name + sequence + "@example.com")
                .name(name)
                .oauthId("oauth-" + sequence)
                .oauthType(OauthType.KAKAO)
                .build());
    }

    private Destination persistDestination() {
        return entityManager.persist(Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .build());
    }

    private Journey persistJourney(
            User postOwner,
            User member,
            Destination destination,
            LocalDate startDate,
            LocalDate endDate,
            boolean activeMember,
            boolean deletedPost
    ) {
        sequence++;
        Post post = entityManager.persist(Post.createPost(
                postOwner,
                destination,
                "제주 여행 " + sequence,
                "JourneyRepository 통합 테스트를 위한 충분히 긴 본문입니다.",
                startDate,
                endDate,
                4,
                startDate.minusDays(1),
                Gender.U,
                true,
                null,
                null,
                "https://example.com/journey-" + sequence + ".png",
                "[]",
                null
        ));
        if (deletedPost) {
            post.softDelete(postOwner.getId(), NOW);
        }

        Journey journey = entityManager.persist(Journey.create(post));
        JourneyMember journeyMember = postOwner.getId().equals(member.getId())
                ? JourneyMember.createHost(journey, member, NOW)
                : JourneyMember.createMember(journey, member, NOW);
        entityManager.persist(journeyMember);
        if (!activeMember) {
            journeyMember.remove(NOW.plusMinutes(1));
        }
        return journey;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
