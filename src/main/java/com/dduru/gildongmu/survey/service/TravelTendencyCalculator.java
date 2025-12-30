package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TravelTendencyCalculator {

    private static final double INITIAL_SCORE = 5.0;
    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 10.0;

    public TendencyScores calculate(Survey survey) {
        double r = INITIAL_SCORE;  // 여행 리듬
        double w = INITIAL_SCORE;  // 지갑 성향
        double s = INITIAL_SCORE;  // 동행 스타일
        double p = INITIAL_SCORE;  // 활동 에너지

        // Q1. 이동수단
        if (survey.getQ1Transport() == Question1Transport.WALK_BUS) {
            w -= 1.0;
            p += 1.0;
        } else if (survey.getQ1Transport() == Question1Transport.TAXI) {
            w += 1.0;
            p -= 1.0;
        }

        // Q2. 웨이팅
        if (survey.getQ2Waiting() == Question2Waiting.WAIT) {
            r += 1.0;
            p += 1.0;
        } else if (survey.getQ2Waiting() == Question2Waiting.MOVE_ELSEWHERE) {
            r -= 1.0;
            p -= 1.0;
        }

        // Q3. 숙소
        if (survey.getQ3Stay() == Question3Stay.HOTEL) {
            r -= 1.0;
            w += 1.5;
        } else if (survey.getQ3Stay() == Question3Stay.JUST_SLEEP) {
            r += 1.0;
            w -= 1.5;
        }

        // Q4. 기상시간
        if (survey.getQ4Wakeup() == Question4Wakeup.EARLY) {
            p += 2.0;
        } else if (survey.getQ4Wakeup() == Question4Wakeup.RELAXED) {
            p -= 2.0;
        }

        // Q5. 경비관리
        if (survey.getQ5Expense() == Question5Expense.EACH_PAYS) {
            w -= 1.0;
            s -= 0.5;
        } else if (survey.getQ5Expense() == Question5Expense.POOLED) {
            w += 1.0;
            s += 0.5;
        }

        // Q6. 소비태도
        if (survey.getQ6Spend() == Question6Spend.SPLURGE) {
            w += 2.0;
        } else if (survey.getQ6Spend() == Question6Spend.SAVE) {
            w -= 2.0;
        }

        // Q7. 선호활동 (택3 누적합산)
        List<Question7Interest> interests = survey.getQ7Interests();
        if (interests != null) {
            for (Question7Interest interest : interests) {
                switch (interest) {
                    case SIGHTSEEING:
                        p += 0.5;
                        break;
                    case EXHIBITION:
                        p -= 0.5;
                        break;
                    case NATURE:
                        p -= 0.5;
                        break;
                    case FOOD:
                        w += 0.5;
                        break;
                    case SHOPPING:
                        w += 1.0;
                        break;
                    case RESORT:
                        p -= 1.0;
                        break;
                    case ACTIVITY:
                        r += 1.0;
                        p += 0.5;
                        break;
                    case THEME_PARK:
                        r += 0.5;
                        p += 0.5;
                        break;
                    case FESTIVAL:
                        r += 1.0;
                        p += 1.0;
                        break;
                }
            }
        }

        // Q8. 계획성
        if (survey.getQ8Planning() == Question8Planning.DETAILED) {
            r -= 2.0;
        } else if (survey.getQ8Planning() == Question8Planning.FLEXIBLE) {
        } else if (survey.getQ8Planning() == Question8Planning.ON_SITE) {
            r += 2.0;
        }

        // Q9. 낯선메뉴
        if (survey.getQ9Menu() == Question9Menu.SAFE) {
            r -= 1.5;
        } else if (survey.getQ9Menu() == Question9Menu.CHECK_REVIEW) {
            r -= 0.5;
        } else if (survey.getQ9Menu() == Question9Menu.CHALLENGE) {
            r += 1.5;
        }

        // Q10. 동행제안
        if (survey.getQ10Companion() == Question10Companion.WELCOME) {
            s += 3.0;
        } else if (survey.getQ10Companion() == Question10Companion.SITUATIONAL) {
            s += 1.0;
        } else if (survey.getQ10Companion() == Question10Companion.US_ONLY) {
            s -= 2.0;
        }

        // Q11. 사진
        if (survey.getQ11Photo() == Question11Photo.LIFETIME_SHOT) {
            s += 1.0;
            p += 1.0;
        } else if (survey.getQ11Photo() == Question11Photo.MATCH_COMPANION) {
            s += 0.5;
        } else if (survey.getQ11Photo() == Question11Photo.EYES_ONLY) {
            s -= 1.0;
            p -= 1.0;
        }

        r = clamp(r);
        w = clamp(w);
        s = clamp(s);
        p = clamp(p);

        return new TendencyScores(r, w, s, p);
    }

    private double clamp(double value) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, value));
    }

    public record TendencyScores(double r, double w, double s, double p) {
    }
}
