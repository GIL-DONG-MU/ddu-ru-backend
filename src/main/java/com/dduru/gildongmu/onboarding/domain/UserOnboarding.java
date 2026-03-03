package com.dduru.gildongmu.onboarding.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_onboardings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOnboarding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private boolean isOnboardingCompleted;
    private boolean isProfileCompleted;

    @Enumerated(value = EnumType.STRING)
    private SurveyStatus surveyStatus = SurveyStatus.NOT_STARTED;

    public UserOnboarding(User user) {
        this.user = user;
    }

    public void completeOnboarding() {
        this.isOnboardingCompleted = true;
    }

    public void completeProfile(){
        this.isProfileCompleted = true;
    }

    public void completeSurvey(){
        this.surveyStatus = SurveyStatus.COMPLETED;
    }

    public void skipSurvey(){
        this.surveyStatus = SurveyStatus.SKIPPED;
    }
}
