package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;

public interface SurveyOptionSpec extends CodedEnum {
    String getIcon();
    String getDescription();

    @Override
    default String getText() {
        return getDescription();
    }
}
