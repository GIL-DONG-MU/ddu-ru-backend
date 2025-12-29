package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.survey.converter.SurveyConverter;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.SurveyRequest;
import com.dduru.gildongmu.survey.dto.SurveyResponse;
import com.dduru.gildongmu.survey.exception.SurveyResultNotFoundException;
import com.dduru.gildongmu.survey.repository.SurveyRepository;
import com.dduru.gildongmu.survey.repository.TravelTendencyRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("설문조사 서비스 테스트")
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;
    @Mock
    private TravelTendencyRepository travelTendencyRepository;
    @Mock
    private SurveyConverter surveyConverter;
    @Mock
    private TravelTendencyCalculator tendencyCalculator;
    @Mock
    private AvatarMatcher avatarMatcher;
    @Mock
    private AvatarProfileProvider avatarProfileProvider;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SurveyService surveyService;

    private User testUser;
    private SurveyRequest testRequest;
    private Survey testSurvey;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .name("테스트")
                .profileImage("http://example.com/image.jpg")
                .oauthId("12345")
                .oauthType(com.dduru.gildongmu.user.enums.OauthType.KAKAO)
                .build();

        testRequest = new SurveyRequest(
                1, 1, 1, 1, 1, 1,
                List.of(1, 2, 3),
                1, 1, 1, 1
        );

        testSurvey = Survey.createSurvey(
                testUser,
                Question1Transport.WALK_BUS,
                Question2Waiting.WAIT,
                Question3Stay.HOTEL,
                Question4Wakeup.EARLY,
                Question5Expense.EACH_PAYS,
                Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.EXHIBITION, Question7Interest.NATURE),
                Question8Planning.DETAILED,
                Question9Menu.SAFE,
                Question10Companion.SITUATIONAL,
                Question11Photo.LIFETIME_SHOT
        );
    }

    @Test
    @DisplayName("새로운_설문_제출_성공")
    void 새로운_설문_제출_성공() {
        // given
        SurveyConverter.ParsedSurveyData parsedData = new SurveyConverter.ParsedSurveyData(
                Question1Transport.WALK_BUS,
                Question2Waiting.WAIT,
                Question3Stay.HOTEL,
                Question4Wakeup.EARLY,
                Question5Expense.EACH_PAYS,
                Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.EXHIBITION, Question7Interest.NATURE),
                Question8Planning.DETAILED,
                Question9Menu.SAFE,
                Question10Companion.SITUATIONAL,
                Question11Photo.LIFETIME_SHOT
        );

        when(userRepository.getByIdOrThrow(1L)).thenReturn(testUser);
        when(surveyRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(surveyConverter.parseRequest(testRequest)).thenReturn(parsedData);
        when(surveyConverter.toEntity(testUser, testRequest)).thenReturn(testSurvey);
        when(surveyRepository.save(any(Survey.class))).thenReturn(testSurvey);

        TravelTendencyCalculator.TendencyScores scores =
                new TravelTendencyCalculator.TendencyScores(5.5, 6.0, 7.0, 4.5);
        when(tendencyCalculator.calculate(testSurvey)).thenReturn(scores);
        when(avatarMatcher.match(scores.r(), scores.w(), scores.s())).thenReturn(AvatarType.TTUR_SWEET);

        AvatarProfileProvider.AvatarProfile profile = new AvatarProfileProvider.AvatarProfile(
                "성격", "강점", "팁"
        );
        when(avatarProfileProvider.getProfile(AvatarType.TTUR_SWEET)).thenReturn(profile);

        when(travelTendencyRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(travelTendencyRepository.save(any(TravelTendency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SurveyResponse response = surveyService.submitSurvey(1L, testRequest);

        // then
        assertThat(response.r()).isEqualTo(5.5);
        assertThat(response.w()).isEqualTo(6.0);
        assertThat(response.s()).isEqualTo(7.0);
        assertThat(response.p()).isEqualTo(4.5);
        assertThat(response.avatarType()).isEqualTo("TTUR_SWEET");
        assertThat(response.personality()).isEqualTo("성격");
        assertThat(response.strength()).isEqualTo("강점");
        assertThat(response.tip()).isEqualTo("팁");

        verify(surveyRepository).save(any(Survey.class));
        verify(travelTendencyRepository).save(any(TravelTendency.class));
    }

    @Test
    @DisplayName("기존_설문_업데이트_성공")
    void 기존_설문_업데이트_성공() {
        // given
        Survey existingSurvey = Survey.createSurvey(
                testUser,
                Question1Transport.TAXI,
                Question2Waiting.MOVE_ELSEWHERE,
                Question3Stay.JUST_SLEEP,
                Question4Wakeup.RELAXED,
                Question5Expense.POOLED,
                Question6Spend.SPLURGE,
                List.of(Question7Interest.FOOD, Question7Interest.SHOPPING, Question7Interest.ACTIVITY),
                Question8Planning.ON_SITE,
                Question9Menu.CHALLENGE,
                Question10Companion.WELCOME,
                Question11Photo.EYES_ONLY
        );

        SurveyConverter.ParsedSurveyData parsedData = new SurveyConverter.ParsedSurveyData(
                Question1Transport.WALK_BUS,
                Question2Waiting.WAIT,
                Question3Stay.HOTEL,
                Question4Wakeup.EARLY,
                Question5Expense.EACH_PAYS,
                Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.EXHIBITION, Question7Interest.NATURE),
                Question8Planning.DETAILED,
                Question9Menu.SAFE,
                Question10Companion.SITUATIONAL,
                Question11Photo.LIFETIME_SHOT
        );

        when(userRepository.getByIdOrThrow(1L)).thenReturn(testUser);
        when(surveyRepository.findByUser(testUser)).thenReturn(Optional.of(existingSurvey));
        when(surveyConverter.parseRequest(testRequest)).thenReturn(parsedData);

        TravelTendencyCalculator.TendencyScores scores =
                new TravelTendencyCalculator.TendencyScores(6.0, 7.5, 8.0, 5.0);
        when(tendencyCalculator.calculate(existingSurvey)).thenReturn(scores);
        when(avatarMatcher.match(scores.r(), scores.w(), scores.s())).thenReturn(AvatarType.TTUR_PADO);

        AvatarProfileProvider.AvatarProfile profile = new AvatarProfileProvider.AvatarProfile(
                "성격2", "강점2", "팁2"
        );
        when(avatarProfileProvider.getProfile(AvatarType.TTUR_PADO)).thenReturn(profile);

        TravelTendency existingTendency = TravelTendency.create(
                testUser,
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(6.0),
                BigDecimal.valueOf(7.0),
                BigDecimal.valueOf(4.0),
                AvatarType.TTUR_SWEET
        );
        when(travelTendencyRepository.findByUser(testUser)).thenReturn(Optional.of(existingTendency));

        // when
        SurveyResponse response = surveyService.submitSurvey(1L, testRequest);

        // then
        assertThat(response.avatarType()).isEqualTo("TTUR_PADO");
        verify(surveyRepository, never()).save(any(Survey.class));
    }

    @Test
    @DisplayName("존재하지_않는_사용자_예외발생")
    void 존재하지_않는_사용자_예외발생() {
        // given
        when(userRepository.getByIdOrThrow(999L)).thenThrow(UserNotFoundException.of(999L));

        // when & then
        assertThatThrownBy(() -> surveyService.submitSurvey(999L, testRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(surveyRepository, never()).save(any());
        verify(travelTendencyRepository, never()).save(any());
    }

    @Test
    @DisplayName("설문_결과_조회_성공")
    void 설문_결과_조회_성공() {
        // given
        TravelTendency travelTendency = TravelTendency.create(
                testUser,
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(6.0),
                BigDecimal.valueOf(7.0),
                BigDecimal.valueOf(4.5),
                AvatarType.TTUR_SWEET
        );

        when(userRepository.getByIdOrThrow(1L)).thenReturn(testUser);
        when(travelTendencyRepository.findByUser(testUser)).thenReturn(Optional.of(travelTendency));

        AvatarProfileProvider.AvatarProfile profile = new AvatarProfileProvider.AvatarProfile(
                "성격", "강점", "팁"
        );
        when(avatarProfileProvider.getProfile(AvatarType.TTUR_SWEET)).thenReturn(profile);

        // when
        SurveyResponse response = surveyService.getMySurveyResult(1L);

        // then
        assertThat(response.r()).isEqualTo(5.5);
        assertThat(response.w()).isEqualTo(6.0);
        assertThat(response.s()).isEqualTo(7.0);
        assertThat(response.p()).isEqualTo(4.5);
        assertThat(response.avatarType()).isEqualTo("TTUR_SWEET");
        assertThat(response.avatarCode()).isEqualTo(4);
    }

    @Test
    @DisplayName("설문_결과_없을때_예외발생")
    void 설문_결과_없을때_예외발생() {
        // given
        when(userRepository.getByIdOrThrow(1L)).thenReturn(testUser);
        when(travelTendencyRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> surveyService.getMySurveyResult(1L))
                .isInstanceOf(SurveyResultNotFoundException.class);
    }
}
