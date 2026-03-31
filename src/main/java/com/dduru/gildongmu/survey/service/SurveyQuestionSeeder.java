package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.S3.service.S3Service;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyQuestionSeeder implements ApplicationRunner {

    private static final String SURVEY_VERSION = "1.0.0";

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final ObjectProvider<S3Service> s3ServiceProvider;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProd = java.util.Arrays.asList(activeProfiles).contains("prod");
        
        if (isProd) {
            log.info("프로덕션 환경에서는 자동 시딩이 비활성화되어 있습니다. 필요시 수동으로 시딩을 실행하세요.");
            return;
        }

        if (surveyQuestionRepository.count() > 0) {
            return;
        }

        log.info("설문 문항 초기 데이터 시딩 시작 (버전: {})", SURVEY_VERSION);

        List<QuestionSeed> seeds = List.of(
                buildSingle(
                        1, "q1",
                        "다음 여행지로 출발~\n어떻게 가지?",
                        imageKey("q1"),
                        Question1Transport.values()
                ),
                buildSingle(
                        2, "q2",
                        "헉! 유명한 맛집인데..\n대기가 2시간😵‍💫",
                        imageKey("q2"),
                        Question2Waiting.values()
                ),
                buildSingle(
                        3, "q3",
                        "여행 준비 완료!😆\n그런데 숙소는...",
                        imageKey("q3"),
                        Question3Stay.values()
                ),
                buildSingle(
                        4, "q4",
                        "여행지에서의 하루!\n어떻게 보내는 게 좋을까?",
                        imageKey("q4"),
                        Question4Wakeup.values()
                ),
                buildSingle(
                        5, "q5",
                        "돈 관리는 중요한데...\n우리 경비는 어떻게 할까?",
                        imageKey("q5"),
                        Question5Expense.values()
                ),
                buildSingle(
                        6, "q6",
                        "여행지에서 지갑을 여는 순간!💸\n나는...",
                        imageKey("q6"),
                        Question6Spend.values()
                ),
                buildMulti(
                        7, "q7",
                        "여행 가서 주로 뭐 하고 싶어?\n(최대 3개)",
                        imageKey("q7"),
                        1, 3,
                        Question7Interest.values()
                ),
                buildSingle(
                        8, "q8",
                        "여행 계획 좀 세워볼까?",
                        imageKey("q8"),
                        Question8Planning.values()
                ),
                buildSingle(
                        9, "q9",
                        "갑자기 특이한 메뉴를 추천받았다!",
                        imageKey("q9"),
                        Question9Menu.values()
                ),
                buildSingle(
                        10, "q10",
                        "친구의 지인이 동행하자고 한다면?",
                        imageKey("q10"),
                        Question10Companion.values()
                ),
                buildSingle(
                        11, "q11",
                        "기억하고 싶은 순간!",
                        imageKey("q11"),
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
        log.info("설문 문항 초기 데이터 시딩 완료 - count: {}, version: {}", questions.size(), SURVEY_VERSION);
    }

    private record QuestionSeed(SurveyQuestion question, List<SurveyQuestionOption> options) {
    }

    private String imageKey(String questionId) {
        String key = "survey/" + questionId + ".png";
        S3Service s3Service = s3ServiceProvider.getIfAvailable();
        if (s3Service == null) {
            return key;
        }
        return s3Service.getS3Url(key);
    }

    private <E extends Enum<E> & SurveyOptionSpec> QuestionSeed buildSingle(int order, String questionId,
                                                                             String questionText, String imageUrl,
                                                                             E[] options) {
        SurveyQuestion question = SurveyQuestion.createQuestion(
                questionId, order, SurveyQuestionType.SINGLE, true,
                1, 1, questionText, imageUrl
        );

        List<SurveyQuestionOption> optionEntities = createOptions(question, options);
        return new QuestionSeed(question, optionEntities);
    }

    private <E extends Enum<E> & SurveyOptionSpec> QuestionSeed buildMulti(int order, String questionId,
                                                                             String questionText, String imageUrl,
                                                                             int minSelect, int maxSelect,
                                                                             E[] options) {
        SurveyQuestion question = SurveyQuestion.createQuestion(
                questionId, order, SurveyQuestionType.MULTI, true,
                minSelect, maxSelect, questionText, imageUrl
        );

        List<SurveyQuestionOption> optionEntities = createOptions(question, options);
        return new QuestionSeed(question, optionEntities);
    }

    private <E extends Enum<E> & SurveyOptionSpec> List<SurveyQuestionOption> createOptions(
            SurveyQuestion question, E[] options) {
        int displayOrder = 1;
        List<SurveyQuestionOption> optionEntities = new ArrayList<>();

        for (E opt : options) {
            optionEntities.add(SurveyQuestionOption.createOption(
                    question,
                    displayOrder++,
                    opt.getCode(),
                    opt.getIcon(),
                    opt.getDescription()
            ));
        }
        return optionEntities;
    }
}
