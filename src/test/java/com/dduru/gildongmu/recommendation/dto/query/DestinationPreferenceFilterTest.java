package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DestinationPreferenceFilter 테스트")
class DestinationPreferenceFilterTest {

    @Test
    @DisplayName("잘못 저장된 null 여행지 값은 필터 조건에서 무시한다")
    void ignoresNullPreferenceValues() {
        DestinationPreferenceFilter filter = DestinationPreferenceFilter.from(List.of(
                new DestinationPreferenceFilterRow(RecommendationDestinationPreferenceType.COUNTRY, "KR", null),
                new DestinationPreferenceFilterRow(RecommendationDestinationPreferenceType.COUNTRY, null, null),
                new DestinationPreferenceFilterRow(RecommendationDestinationPreferenceType.CITY, null, 10L),
                new DestinationPreferenceFilterRow(RecommendationDestinationPreferenceType.CITY, null, null)
        ));

        assertThat(filter.countryCodes()).containsExactly("KR");
        assertThat(filter.destinationIds()).containsExactly(10L);
    }
}
