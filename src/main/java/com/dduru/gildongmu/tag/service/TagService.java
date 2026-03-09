package com.dduru.gildongmu.tag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private static final List<String> TRAVEL_TAGS = List.of(
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

    public List<String> getPopularTags() {
        return TRAVEL_TAGS;
    }
}
