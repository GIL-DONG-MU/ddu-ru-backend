package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.TravelSurvey;
import com.dduru.gildongmu.survey.domain.TravelSurveyInterest;
import com.dduru.gildongmu.survey.domain.enums.Interest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TravelSurveyInterestConverter {

    public List<TravelSurveyInterest> toEntity(TravelSurvey travelSurvey, List<String> interests) {
        return interests.stream()
                .map(interest -> TravelSurveyInterest.create(
                        travelSurvey,
                        Interest.valueOf(interest)
                ))
                .toList();
    }
}
