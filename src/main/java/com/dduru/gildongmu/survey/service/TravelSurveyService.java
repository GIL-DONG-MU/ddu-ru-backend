package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.converter.TravelSurveyConverter;
import com.dduru.gildongmu.survey.converter.TravelSurveyInterestConverter;
import com.dduru.gildongmu.survey.domain.TravelSurvey;
import com.dduru.gildongmu.survey.domain.TravelSurveyInterest;
import com.dduru.gildongmu.survey.dto.TravelSurveyRequest;
import com.dduru.gildongmu.survey.repository.TravelSurveyInterestRepository;
import com.dduru.gildongmu.survey.repository.TravelSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TravelSurveyService {

    private final TravelSurveyRepository travelSurveyRepository;
    private final TravelSurveyInterestRepository travelSurveyInterestRepository;

    private final TravelSurveyConverter travelSurveyConverter;
    private final TravelSurveyInterestConverter travelSurveyInterestConverter;

    public void create(Long userId, TravelSurveyRequest request) {
        TravelSurvey surveyRequest = travelSurveyConverter.toEntity(userId, request);
        TravelSurvey travelSurvey = travelSurveyRepository.findByUserId(userId)
                .map(existingSurvey -> {
                    existingSurvey.updateStyles(
                            surveyRequest.getPlanStyle(),
                            surveyRequest.getTastingStyle(),
                            surveyRequest.getStayStyle(),
                            surveyRequest.getExpenseStyle(),
                            surveyRequest.getMoveStyle(),
                            surveyRequest.getSpendStyle(),
                            surveyRequest.getCaptureStyle(),
                            surveyRequest.getPaceStyle()
                    );
                    travelSurveyRepository.save(existingSurvey);
                    travelSurveyInterestRepository.deleteAll(travelSurveyInterestRepository.findByTravelSurvey(existingSurvey));
                    return existingSurvey;
                })
                .orElseGet(() -> travelSurveyRepository.save(surveyRequest));

        List<TravelSurveyInterest> interests = travelSurveyInterestConverter.toEntity(travelSurvey, request.interests());
        travelSurveyInterestRepository.saveAll(interests);
    }
}
