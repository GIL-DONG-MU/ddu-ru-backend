package com.dduru.gildongmu.tag.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 테스트")
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @DisplayName("여행 태그 목록을 조회하면 고정된 10개 목록을 반환한다")
    @Test
    void getPopularTags_returnsFixedTravelTagList() {
        List<String> result = tagService.getPopularTags();

        assertThat(result).containsExactly(
                "맛집투어",
                "사진명소",
                "힐링",
                "액티비티",
                "현지바이브",
                "야경명소",
                "카페투어",
                "커피",
                "쇼핑",
                "뚜벅이"
        );
    }
}
