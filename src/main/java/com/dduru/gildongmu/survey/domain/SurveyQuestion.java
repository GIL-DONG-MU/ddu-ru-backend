package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.SurveyQuestionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "survey_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestion extends BaseTimeEntity {

    @Id
    @Column(name = "question_id", nullable = false, length = 20)
    private String questionId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SurveyQuestionType type;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "min_select")
    private Integer minSelect;

    @Column(name = "max_select")
    private Integer maxSelect;

    @Column(name = "text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder
    public SurveyQuestion(String questionId, int displayOrder, SurveyQuestionType type, boolean required,
                          Integer minSelect, Integer maxSelect, String questionText, String imageUrl) {
        this.questionId = questionId;
        this.displayOrder = displayOrder;
        this.type = type;
        this.required = required;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.questionText = questionText;
        this.imageUrl = imageUrl;
    }

    public static SurveyQuestion createQuestion(String questionId, int displayOrder, SurveyQuestionType type, boolean required,
                                                Integer minSelect, Integer maxSelect, String questionText, String imageUrl) {
        return SurveyQuestion.builder()
                .questionId(questionId)
                .displayOrder(displayOrder)
                .type(type)
                .required(required)
                .minSelect(minSelect)
                .maxSelect(maxSelect)
                .questionText(questionText)
                .imageUrl(imageUrl)
                .build();
    }

    public void updateQuestion(int displayOrder, SurveyQuestionType type, boolean required,
                               Integer minSelect, Integer maxSelect, String questionText, String imageUrl) {
        this.displayOrder = displayOrder;
        this.type = type;
        this.required = required;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.questionText = questionText;
        this.imageUrl = imageUrl;
    }
}
