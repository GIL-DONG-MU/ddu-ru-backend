package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "survey_question_options",
        uniqueConstraints = @UniqueConstraint(name = "uk_question_code", columnNames = {"question_id", "code"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestionOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "text", nullable = false, length = 255)
    private String text;

    @Builder
    public SurveyQuestionOption(SurveyQuestion question, int displayOrder, int code, String icon, String text) {
        this.question = question;
        this.displayOrder = displayOrder;
        this.code = code;
        this.icon = icon;
        this.text = text;
    }

    public static SurveyQuestionOption createOption(SurveyQuestion question, int displayOrder, int code, String icon, String text) {
        return SurveyQuestionOption.builder()
                .question(question)
                .displayOrder(displayOrder)
                .code(code)
                .icon(icon)
                .text(text)
                .build();
    }
}
