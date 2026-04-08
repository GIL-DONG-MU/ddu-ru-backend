package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.service.ProfileManagementService;
import com.dduru.gildongmu.survey.converter.SurveyConverter;
import com.dduru.gildongmu.survey.converter.ParsedSurveyData;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.response.AvatarProfileResponse;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.dto.response.SurveyResponse;
import com.dduru.gildongmu.survey.dto.response.TendencyScoreResponse;
import com.dduru.gildongmu.survey.exception.SurveyResultNotFoundException;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import com.dduru.gildongmu.survey.repository.SurveyRepository;
import com.dduru.gildongmu.survey.repository.TravelTendencyRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private AvatarProfileService avatarProfileService;
    @Mock
    private AvatarProfileRepository avatarProfileRepository;
    @Mock
    private ProfileManagementService profileManagementService;
    @Mock
    private OnboardingService onboardingService;
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
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();

        testRequest = new SurveyRequest(
                1, 1, 1,
                List.of(1, 2, 3),
                1, 1, 1,
                1, 1, 1,
                1, 1, 1,
                1
        );

        ReflectionTestUtils.setField(testUser, "id", 1L);

        testSurvey = Survey.createSurvey(
                testUser,
                RhythmQuestion1.PLANNED_ROUTE,
                RhythmQuestion2.PACK_EARLY,
                RhythmQuestion3.ROUTE_TIME_SET,
                ConsumptionQuestion1.ADJUST_BUDGET,
                ConsumptionQuestion2.VALUE_TRANSPORT,
                ConsumptionQuestion3.VALUE_CHOICE,
                EnergyQuestion1.RELAXED_DAY,
                EnergyQuestion2.BRUNCH_INSTEAD,
                EnergyQuestion3.DO_NOTHING_OK,
                DecisionQuestion1.DELEGATE_ROLE,
                DecisionQuestion2.FOLLOW_OTHERS,
                DecisionQuestion3.WAIT_AND_SEE,
                RecordStyleQuestion.EYES_FIRST,
                List.of(ActivityTag.SIGHTSEEING, ActivityTag.EXHIBITION, ActivityTag.NATURE)
        );
    }

    @Test
    @DisplayName("새로운_설문_제출_성공")
    void 새로운_설문_제출_성공() {
        ParsedSurveyData parsedData = new ParsedSurveyData(
                RhythmQuestion1.PLANNED_ROUTE,
                RhythmQuestion2.PACK_EARLY,
                RhythmQuestion3.ROUTE_TIME_SET,
                ConsumptionQuestion1.ADJUST_BUDGET,
                ConsumptionQuestion2.VALUE_TRANSPORT,
                ConsumptionQuestion3.VALUE_CHOICE,
                EnergyQuestion1.RELAXED_DAY,
                EnergyQuestion2.BRUNCH_INSTEAD,
                EnergyQuestion3.DO_NOTHING_OK,
                DecisionQuestion1.DELEGATE_ROLE,
                DecisionQuestion2.FOLLOW_OTHERS,
                DecisionQuestion3.WAIT_AND_SEE,
                RecordStyleQuestion.EYES_FIRST,
                List.of(ActivityTag.SIGHTSEEING, ActivityTag.EXHIBITION, ActivityTag.NATURE)
        );

        when(userRepository.getByIdOrThrow(1L)).thenReturn(testUser);
        when(surveyRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(surveyConverter.parseRequest(testRequest)).thenReturn(parsedData);
        when(surveyConverter.toEntity(testUser, parsedData)).thenReturn(testSurvey);
        when(surveyRepository.save(any(Survey.class))).thenReturn(testSurvey);

        TendencyScoreResponse scores =
                new TendencyScoreResponse(5.5, 4.5, 6.0, 7.0);
        when(tendencyCalculator.calculate(testSurvey)).thenReturn(scores);
        when(avatarMatcher.match(scores.rhythmScore(), scores.energyScore(), scores.consumptionScore(), scores.decisionScore()))
                .thenReturn(AvatarType.TTUR_SWEET);

        AvatarProfileResponse profile = new AvatarProfileResponse(
                "뚜르 스윗",
                "설명",
                List.of("태그1", "태그2", "태그3"),
                "성격\n\n강점\n\n팁",
                "https://example.com/avatar-sweet.png"
        );
        when(avatarProfileService.getProfile(AvatarType.TTUR_SWEET)).thenReturn(profile);

        AvatarProfile avatarProfile = AvatarProfile.builder()
                .avatarType(AvatarType.TTUR_SWEET)
                .displayName("뚜르 스윗")
                .oneLineDescription("설명")
                .body("성격\n\n강점\n\n팁")
                .imageUrl("https://example.com/avatar-sweet.png")
                .tags("[]")
                .build();
        ReflectionTestUtils.setField(avatarProfile, "id", 1L);
        when(avatarProfileRepository.findByAvatarType(AvatarType.TTUR_SWEET)).thenReturn(Optional.of(avatarProfile));

        when(travelTendencyRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(travelTendencyRepository.save(any(TravelTendency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SurveyResponse response = surveyService.submitSurvey(1L, testRequest);

        assertThat(response.rhythmScore()).isEqualTo(5.5);
        assertThat(response.energyScore()).isEqualTo(4.5);
        assertThat(response.consumptionScore()).isEqualTo(6.0);
        assertThat(response.decisionScore()).isEqualTo(7.0);
        assertThat(response.avatarType()).isEqualTo(AvatarType.TTUR_SWEET);
        assertThat(response.avatar().characterName()).isEqualTo("뚜르 스윗");
        assertThat(response.avatar().oneLineDescription()).isEqualTo("설명");
        assertThat(response.avatar().tags()).containsExactly("태그1", "태그2", "태그3");
        assertThat(response.avatar().description()).isEqualTo("성격\n\n강점\n\n팁");
        assertThat(response.avatar().imageUrl()).isEqualTo("https://example.com/avatar-sweet.png");

        verify(surveyRepository).save(any(Survey.class));
        verify(travelTendencyRepository).save(any(TravelTendency.class));
    }

    @Test
    @DisplayName("기존_설문_업데이트_성공")
    void 기존_설문_업데이트_성공() {
        Survey existingSurvey = Survey.createSurvey(
                testUser,
                RhythmQuestion1.IMPULSE_SIDE_TRIP,
                RhythmQuestion2.PACK_LAST_MINUTE,
                RhythmQuestion3.ROUGH_LIST_ONLY,
                ConsumptionQuestion1.FLEX_OK,
                ConsumptionQuestion2.SAVE_TIME_TAXI,
                ConsumptionQuestion3.INVEST_EXPERIENCE,
                EnergyQuestion1.PACKED_DAY,
                EnergyQuestion2.BREAKFAST_SPRINT,
                EnergyQuestion3.FILL_WITH_SPOTS,
                DecisionQuestion1.LEAD_OR_ORGANIZE,
                DecisionQuestion2.PROPOSE_FIRST,
                DecisionQuestion3.DRIVE_CONCLUSION,
                RecordStyleQuestion.SHOOT_NOW,
                List.of(ActivityTag.FOOD, ActivityTag.SHOPPING, ActivityTag.ACTIVITY)
        );

        ParsedSurveyData parsedData = new ParsedSurveyData(
                RhythmQuestion1.PLANNED_ROUTE,
                RhythmQuestion2.PACK_EARLY,
                RhythmQuestion3.ROUTE_TIME_SET,
                ConsumptionQuestion1.ADJUST_BUDGET,
                ConsumptionQuestion2.VALUE_TRANSPORT,
                ConsumptionQuestion3.VALUE_CHOICE,
                EnergyQuestion1.RELAXED_DAY,
                EnergyQuestion2.BRUNCH_INSTEAD,
                EnergyQuestion3.DO_NOTHING_OK,
                DecisionQuestion1.DELEGATE_ROLE,
                DecisionQuestion2.FOLLOW_OTHERS,
                DecisionQuestion3.WAIT_AND_SEE,
                RecordStyleQuestion.EYES_FIRST,
                List.of(ActivityTag.SIGHTSEEING, ActivityTag.EXHIBITION, ActivityTag.NATURE)
        );

        when(userRepository.getByIdOrThrow(1L)).thenReturn(testUser);
        when(surveyRepository.findByUser_Id(1L)).thenReturn(Optional.of(existingSurvey));
        when(surveyConverter.parseRequest(testRequest)).thenReturn(parsedData);

        TendencyScoreResponse scores =
                new TendencyScoreResponse(6.0, 5.0, 7.5, 8.0);
        when(tendencyCalculator.calculate(existingSurvey)).thenReturn(scores);
        when(avatarMatcher.match(scores.rhythmScore(), scores.energyScore(), scores.consumptionScore(), scores.decisionScore()))
                .thenReturn(AvatarType.TTUR_PADO);

        AvatarProfileResponse profile = new AvatarProfileResponse(
                "뚜르 파도",
                "설명2",
                List.of("태그1", "태그2", "태그3"),
                "성격2\n\n강점2\n\n팁2",
                "https://example.com/avatar-pado.png"
        );
        when(avatarProfileService.getProfile(AvatarType.TTUR_PADO)).thenReturn(profile);

        AvatarProfile avatarProfile = AvatarProfile.builder()
                .avatarType(AvatarType.TTUR_PADO)
                .displayName("뚜르 파도")
                .oneLineDescription("설명2")
                .body("성격2\n\n강점2\n\n팁2")
                .imageUrl("https://example.com/avatar-pado.png")
                .tags("[]")
                .build();
        ReflectionTestUtils.setField(avatarProfile, "id", 2L);
        when(avatarProfileRepository.findByAvatarType(AvatarType.TTUR_PADO)).thenReturn(Optional.of(avatarProfile));

        TravelTendency existingTendency = TravelTendency.create(
                testUser,
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(4.0),
                BigDecimal.valueOf(6.0),
                BigDecimal.valueOf(7.0),
                AvatarType.TTUR_SWEET
        );
        when(travelTendencyRepository.findByUser_Id(1L)).thenReturn(Optional.of(existingTendency));

        SurveyResponse response = surveyService.submitSurvey(1L, testRequest);

        assertThat(response.avatarType()).isEqualTo(AvatarType.TTUR_PADO);
        verify(surveyRepository, never()).save(any(Survey.class));
    }

    @Test
    @DisplayName("존재하지_않는_사용자_예외발생")
    void 존재하지_않는_사용자_예외발생() {
        when(userRepository.getByIdOrThrow(999L)).thenThrow(new UserNotFoundException());

        assertThatThrownBy(() -> surveyService.submitSurvey(999L, testRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(surveyRepository, never()).save(any());
        verify(travelTendencyRepository, never()).save(any());
    }

    @Test
    @DisplayName("설문_결과_조회_성공")
    void 설문_결과_조회_성공() {
        TravelTendency travelTendency = TravelTendency.create(
                testUser,
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(4.5),
                BigDecimal.valueOf(6.0),
                BigDecimal.valueOf(7.0),
                AvatarType.TTUR_SWEET
        );

        when(travelTendencyRepository.findByUser_Id(1L)).thenReturn(Optional.of(travelTendency));

        AvatarProfileResponse profile = new AvatarProfileResponse(
                "뚜르 스윗",
                "설명",
                List.of("태그1", "태그2", "태그3"),
                "성격\n\n강점\n\n팁",
                "https://example.com/avatar-sweet.png"
        );
        when(avatarProfileService.getProfile(AvatarType.TTUR_SWEET)).thenReturn(profile);

        SurveyResponse response = surveyService.getMySurveyResult(1L);

        assertThat(response.rhythmScore()).isEqualTo(5.5);
        assertThat(response.energyScore()).isEqualTo(4.5);
        assertThat(response.consumptionScore()).isEqualTo(6.0);
        assertThat(response.decisionScore()).isEqualTo(7.0);
        assertThat(response.avatarType()).isEqualTo(AvatarType.TTUR_SWEET);
        assertThat(response.avatarCode()).isEqualTo(4);
        assertThat(response.avatar().imageUrl()).isEqualTo("https://example.com/avatar-sweet.png");
    }

    @Test
    @DisplayName("설문_결과_없을때_예외발생")
    void 설문_결과_없을때_예외발생() {
        when(travelTendencyRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> surveyService.getMySurveyResult(1L))
                .isInstanceOf(SurveyResultNotFoundException.class);
    }
}
