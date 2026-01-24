package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.S3.service.S3Service;
import com.dduru.gildongmu.common.enums.CodedEnum;
import com.dduru.gildongmu.survey.domain.SurveyQuestion;
import com.dduru.gildongmu.survey.domain.SurveyQuestionOption;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.repository.SurveyQuestionOptionRepository;
import com.dduru.gildongmu.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyQuestionSeeder implements ApplicationRunner {

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final ObjectProvider<S3Service> s3ServiceProvider;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (surveyQuestionRepository.count() > 0) {
            return;
        }

        log.info("설문 문항 초기 데이터 시딩 시작");

        List<QuestionSeed> seeds = List.of(
                buildSingle(
                        1, "q1",
                        "다음 여행지로 출발~\n어떻게 가지?",
                        imageKey("q1"),
                        optionSeedQ1,
                        Question1Transport.values()
                ),
                buildSingle(
                        2, "q2",
                        "헉! 유명한 맛집인데..\n대기가 2시간😵‍💫",
                        imageKey("q2"),
                        optionSeedQ2,
                        Question2Waiting.values()
                ),
                buildSingle(
                        3, "q3",
                        "여행 준비 완료!😆\n그런데 숙소는...",
                        imageKey("q3"),
                        optionSeedQ3,
                        Question3Stay.values()
                ),
                buildSingle(
                        4, "q4",
                        "여행지에서의 하루!\n어떻게 보내는 게 좋을까?",
                        imageKey("q4"),
                        optionSeedQ4,
                        Question4Wakeup.values()
                ),
                buildSingle(
                        5, "q5",
                        "돈 관리는 중요한데...\n우리 경비는 어떻게 할까?",
                        imageKey("q5"),
                        optionSeedQ5,
                        Question5Expense.values()
                ),
                buildSingle(
                        6, "q6",
                        "여행지에서 지갑을 여는 순간!💸\n나는...",
                        imageKey("q6"),
                        optionSeedQ6,
                        Question6Spend.values()
                ),
                buildMulti(
                        7, "q7",
                        "여행 가서 주로 뭐 하고 싶어?\n(최대 3개)",
                        imageKey("q7"),
                        1, 3,
                        optionSeedQ7,
                        new Question7Interest[]{
                                Question7Interest.SIGHTSEEING,
                                Question7Interest.EXHIBITION,
                                Question7Interest.NATURE,
                                Question7Interest.FOOD,
                                Question7Interest.SHOPPING,
                                Question7Interest.RESORT,
                                Question7Interest.ACTIVITY,
                                Question7Interest.FESTIVAL
                        }
                ),
                buildSingle(
                        8, "q8",
                        "여행 계획 좀 세워볼까?",
                        imageKey("q8"),
                        optionSeedQ8,
                        Question8Planning.values()
                ),
                buildSingle(
                        9, "q9",
                        "갑자기 특이한 메뉴를 추천받았다!",
                        imageKey("q9"),
                        optionSeedQ9,
                        Question9Menu.values()
                ),
                buildSingle(
                        10, "q10",
                        "친구의 지인이 동행하자고 한다면?",
                        imageKey("q10"),
                        optionSeedQ10,
                        Question10Companion.values()
                ),
                buildSingle(
                        11, "q11",
                        "기억하고 싶은 순간!",
                        imageKey("q11"),
                        optionSeedQ11,
                        Question11Photo.values()
                )
        );

        List<SurveyQuestion> questions = seeds.stream()
                .map(QuestionSeed::question)
                .toList();
        List<SurveyQuestionOption> options = seeds.stream()
                .flatMap(s -> s.options().stream())
                .toList();

        surveyQuestionRepository.saveAll(questions);
        surveyQuestionOptionRepository.saveAll(options);
        log.info("설문 문항 초기 데이터 시딩 완료 - count: {}", questions.size());
    }

    private record QuestionSeed(SurveyQuestion question, List<SurveyQuestionOption> options) {
    }

    private record OptionSeed(String icon, String text) {
    }

    private String imageKey(String questionId) {
        String key = "survey/" + questionId + ".png";
        S3Service s3Service = s3ServiceProvider.getIfAvailable();
        if (s3Service == null) {
            return key;
        }
        return s3Service.getS3Url(key);
    }

    private final OptionSeedProvider optionSeedQ1 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🚶", "여행은 낭만이지! 걷거나 버스");
        case 2 -> new OptionSeed("🚕", "편한게 최고~ 택시 타는 거 어때?");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ2 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("😤", "이왕이면 기다려서라도 먹어야해!");
        case 2 -> new OptionSeed("😄", "기다리기는 좀... 편하게 주변에서 먹자");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ3 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🏨", "잠은 갖춰진 곳에서 자야지");
        case 2 -> new OptionSeed("😴", "잠만 잘 수 있으면 OK!");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ4 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🌞", "일찍 일어나서 알차게 돌아다니자!");
        case 2 -> new OptionSeed("🐌", "서두르기보단 느긋하게 돌아다니자~");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ5 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("💰", "꼼꼼하게 각자 결제!");
        case 2 -> new OptionSeed("🐷", "한 통장에 모아 함께 쓰기!");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ6 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🤑", "여행갔으면 써야지!");
        case 2 -> new OptionSeed("😊", "아무래도 가성비가 최고지~");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ7 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🏛️", "관광");
        case 2 -> new OptionSeed("⚽", "관람");
        case 3 -> new OptionSeed("🌳", "자연 탐방");
        case 4 -> new OptionSeed("🍤", "먹방");
        case 5 -> new OptionSeed("🛍️", "쇼핑");
        case 6 -> new OptionSeed("🏝️", "휴양");
        case 7 -> new OptionSeed("⛷️", "액티비티");
        case 9 -> new OptionSeed("💃", "페스티벌");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ8 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("📅", "시간별 디테일한 일정표!");
        case 2 -> new OptionSeed("📘", "큰 틀만 잡고 상황 따라 융통성 있게 조정");
        case 3 -> new OptionSeed("📍", "가고 싶은 곳만 정하고 현장에서 결정!");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ9 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🍜", "음... 원래 고른 메뉴가 더 안전하지!");
        case 2 -> new OptionSeed("🔎", "후기부터 확인해야지, 구글맵 출동!");
        case 3 -> new OptionSeed("🍽️", "이럴때 아니면 언제? 바로 가야지!!");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ10 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("🤗", "새 친구 생기면 좋지! 완전 환영!");
        case 2 -> new OptionSeed("🙂", "어떤 사람일까? 잘 맞으면 재밌을 듯!");
        case 3 -> new OptionSeed("🙅‍♂️", "우리끼리 여행이 좋아! 정중히 거절!");
        default -> new OptionSeed(null, fallbackText);
    };

    private final OptionSeedProvider optionSeedQ11 = (qid, code, fallbackText) -> switch (code) {
        case 1 -> new OptionSeed("📸", "사진은 무조건 많이! 인생샷 남기기");
        case 2 -> new OptionSeed("🙂", "흠... 상관 없음! 동행자 스타일에 맞출래");
        case 3 -> new OptionSeed("👀", "사진보단 눈으로 담는 게 진짜지");
        default -> new OptionSeed(null, fallbackText);
    };

    private <E extends Enum<E> & CodedEnum> QuestionSeed buildSingle(int order, String questionId,
                                                                     String questionText, String imageUrl,
                                                                     OptionSeedProvider optionSeedProvider,
                                                                     E[] options) {
        SurveyQuestion question = SurveyQuestion.createQuestion(
                questionId, order, SurveyQuestionType.SINGLE, true,
                1, 1, questionText, imageUrl
        );

        int displayOrder = 1;
        List<SurveyQuestionOption> optionEntities = new java.util.ArrayList<>();
        for (E opt : options) {
            OptionSeed seed = optionSeedProvider.seed(questionId, opt.getCode(), opt.getText());
            optionEntities.add(SurveyQuestionOption.createOption(question, displayOrder++, opt.getCode(), seed.icon(), seed.text()));
        }
        return new QuestionSeed(question, optionEntities);
    }

    private <E extends Enum<E> & CodedEnum> QuestionSeed buildMulti(int order, String questionId,
                                                                    String questionText, String imageUrl,
                                                                    int minSelect, int maxSelect,
                                                                    OptionSeedProvider optionSeedProvider,
                                                                    E[] options) {
        SurveyQuestion question = SurveyQuestion.createQuestion(
                questionId, order, SurveyQuestionType.MULTI, true,
                minSelect, maxSelect, questionText, imageUrl
        );

        int displayOrder = 1;
        List<SurveyQuestionOption> optionEntities = new java.util.ArrayList<>();
        for (E opt : options) {
            OptionSeed seed = optionSeedProvider.seed(questionId, opt.getCode(), opt.getText());
            optionEntities.add(SurveyQuestionOption.createOption(question, displayOrder++, opt.getCode(), seed.icon(), seed.text()));
        }
        return new QuestionSeed(question, optionEntities);
    }

    @FunctionalInterface
    private interface OptionSeedProvider {
        OptionSeed seed(String questionId, int code, String fallbackText);
    }
}
