package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.survey.domain.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "surveys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "q1_transport", nullable = false)
    private Question1Transport q1Transport;

    @Enumerated(EnumType.STRING)
    @Column(name = "q2_waiting", nullable = false)
    private Question2Waiting q2Waiting;

    @Enumerated(EnumType.STRING)
    @Column(name = "q3_stay", nullable = false)
    private Question3Stay q3Stay;

    @Enumerated(EnumType.STRING)
    @Column(name = "q4_wakeup", nullable = false)
    private Question4Wakeup q4Wakeup;

    @Enumerated(EnumType.STRING)
    @Column(name = "q5_expense", nullable = false)
    private Question5Expense q5Expense;

    @Enumerated(EnumType.STRING)
    @Column(name = "q6_spend", nullable = false)
    private Question6Spend q6Spend;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "survey_interests", joinColumns = @JoinColumn(name = "survey_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false)
    private List<Question7Interest> q7Interests;

    @Enumerated(EnumType.STRING)
    @Column(name = "q8_planning", nullable = false)
    private Question8Planning q8Planning;

    @Enumerated(EnumType.STRING)
    @Column(name = "q9_menu", nullable = false)
    private Question9Menu q9Menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "q10_companion", nullable = false)
    private Question10Companion q10Companion;

    @Enumerated(EnumType.STRING)
    @Column(name = "q11_photo", nullable = false)
    private Question11Photo q11Photo;

    @Builder
    public Survey(User user, Question1Transport q1Transport, Question2Waiting q2Waiting,
                  Question3Stay q3Stay, Question4Wakeup q4Wakeup, Question5Expense q5Expense,
                  Question6Spend q6Spend, List<Question7Interest> q7Interests,
                  Question8Planning q8Planning, Question9Menu q9Menu,
                  Question10Companion q10Companion, Question11Photo q11Photo) {
        this.user = user;
        this.q1Transport = q1Transport;
        this.q2Waiting = q2Waiting;
        this.q3Stay = q3Stay;
        this.q4Wakeup = q4Wakeup;
        this.q5Expense = q5Expense;
        this.q6Spend = q6Spend;
        this.q7Interests = q7Interests != null ? new ArrayList<>(q7Interests) : null;
        this.q8Planning = q8Planning;
        this.q9Menu = q9Menu;
        this.q10Companion = q10Companion;
        this.q11Photo = q11Photo;
    }

    public static Survey createSurvey(User user, Question1Transport q1Transport, Question2Waiting q2Waiting,
                                      Question3Stay q3Stay, Question4Wakeup q4Wakeup, Question5Expense q5Expense,
                                      Question6Spend q6Spend, List<Question7Interest> q7Interests,
                                      Question8Planning q8Planning, Question9Menu q9Menu,
                                      Question10Companion q10Companion, Question11Photo q11Photo) {
        return Survey.builder()
                .user(user)
                .q1Transport(q1Transport)
                .q2Waiting(q2Waiting)
                .q3Stay(q3Stay)
                .q4Wakeup(q4Wakeup)
                .q5Expense(q5Expense)
                .q6Spend(q6Spend)
                .q7Interests(q7Interests)
                .q8Planning(q8Planning)
                .q9Menu(q9Menu)
                .q10Companion(q10Companion)
                .q11Photo(q11Photo)
                .build();
    }

    public void updateSurvey(Question1Transport q1Transport, Question2Waiting q2Waiting, Question3Stay q3Stay,
                             Question4Wakeup q4Wakeup, Question5Expense q5Expense, Question6Spend q6Spend,
                             List<Question7Interest> q7Interests, Question8Planning q8Planning,
                             Question9Menu q9Menu, Question10Companion q10Companion, Question11Photo q11Photo) {
        this.q1Transport = q1Transport;
        this.q2Waiting = q2Waiting;
        this.q3Stay = q3Stay;
        this.q4Wakeup = q4Wakeup;
        this.q5Expense = q5Expense;
        this.q6Spend = q6Spend;
        if (this.q7Interests == null) {
            this.q7Interests = q7Interests != null ? new ArrayList<>(q7Interests) : new ArrayList<>();
        } else {
            this.q7Interests.clear();
            if (q7Interests != null) {
                this.q7Interests.addAll(q7Interests);
            }
        }
        this.q8Planning = q8Planning;
        this.q9Menu = q9Menu;
        this.q10Companion = q10Companion;
        this.q11Photo = q11Photo;
    }
}
